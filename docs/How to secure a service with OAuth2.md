# How to Secure a Service with OAuth2

This guide covers adding OAuth2 authentication to both a Spring Boot backend (resource server) and an Angular frontend (PKCE flow), using the existing auth-server.

---

## Part 1: Register a new OAuth2 client on the Auth Server

**File:** `src/backend/infra/auth-server/src/main/resources/application.yml`

Add a new entry under `oauth2.clients`. For a frontend app using PKCE:

```yaml
oauth2:
  clients:
    my-new-client:
      client-id: my-new-client
      client-secret: my-new-secret          # required by config but not used for PKCE
      client-authentication-method: none     # PKCE = no client secret
      authorization-grant-types:
        - authorization_code
        - refresh_token
      redirect-uris:
        - http://shopbuddy.com/<app-path>/login/callback
        - http://localhost:4200/<app-path>/login/callback
      scopes:
        - openid
        - read
      client-settings:
        require-authorization-consent: true
        require-proof-key: true              # enforces PKCE
```

---

## Part 2: Secure the Backend as an OAuth2 Resource Server

### 2.1 Add dependencies

```groovy
// build.gradle
implementation 'org.springframework.boot:spring-boot-starter-security'
implementation 'org.springframework.boot:spring-boot-starter-oauth2-resource-server'
```

### 2.2 Add auth server URL to application.yaml

```yaml
env.base.url.internal.auth: ${INTERNAL_AUTH_SERVER_BASE_URL:http://localhost:8080}
```

### 2.3 Create SecurityConfig.java

Two filter chains following the project convention:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

   @Value("${env.base.url.internal.auth}") String authServerUrl;

   // Public endpoints (actuator, swagger, etc.) — no auth required
   @Bean
   @Order(1)
   public SecurityFilterChain publicFilterChain(HttpSecurity http) throws Exception {
      http.securityMatcher("/actuator/**")
         .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
         .csrf(AbstractHttpConfigurer::disable)
         .authorizeHttpRequests(a -> a.anyRequest().permitAll())
         .oauth2ResourceServer(AbstractHttpConfigurer::disable)
         .exceptionHandling(AbstractHttpConfigurer::disable)
         .anonymous(AbstractHttpConfigurer::disable);
      return http.build();
   }

   // Everything else — requires valid JWT
   @Bean
   @Order(2)
   public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
      http
         .cors(cors -> cors.configurationSource(corsConfigurationSource()))
         .csrf(AbstractHttpConfigurer::disable)
         .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
         .authorizeHttpRequests(a -> a.anyRequest().authenticated())
         .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt
               .jwkSetUri(authServerUrl + "/oauth2/jwks")
               .jwtAuthenticationConverter(jwtAuthenticationConverter())
            )
         );
      return http.build();
   }

   @Bean
   public JwtAuthenticationConverter jwtAuthenticationConverter() {
      JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
      converter.setJwtGrantedAuthoritiesConverter(jwt -> {
         Collection<GrantedAuthority> authorities = new ArrayList<>(
            new JwtGrantedAuthoritiesConverter().convert(jwt)
         );
         // Extract custom "roles" claim -> ROLE_<name>
         List<String> roles = jwt.getClaimAsStringList("roles");
         if (roles != null)
            roles.forEach(r -> authorities.add(new SimpleGrantedAuthority("ROLE_" + r)));
         // Extract custom "permissions" claim -> direct authorities
         List<String> perms = jwt.getClaimAsStringList("permissions");
         if (perms != null)
            perms.forEach(p -> authorities.add(new SimpleGrantedAuthority(p)));
         return authorities;
      });
      return converter;
   }

   @Bean
   public CorsConfigurationSource corsConfigurationSource() {
      CorsConfiguration config = new CorsConfiguration();
      config.setAllowedOriginPatterns(Arrays.asList("*"));
      config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
      config.setAllowedHeaders(Arrays.asList("*"));
      config.setAllowCredentials(true);
      UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
      source.registerCorsConfiguration("/**", config);
      return source;
   }
}
```

### 2.4 Update CORS (WebConfig or Ingress)

Make sure `Authorization` header and the frontend origin are allowed:

**WebConfig.java:**
```java
registry.addMapping("/**")
   .allowedOrigins("http://localhost:4200", "http://shopbuddy.com")
   .allowedMethods("*")
   .allowedHeaders("*")
   .allowCredentials(true);
```

**Ingress annotation:**
```yaml
nginx.ingress.kubernetes.io/cors-allow-origin: "http://localhost:4200, http://shopbuddy.com"
```

### 2.5 K8s Deployment

Add `envFrom` so the pod gets `INTERNAL_AUTH_SERVER_BASE_URL`:

```yaml
envFrom:
  - configMapRef:
      name: backend-url-configmap
```

---

## Part 3: Angular Frontend — OAuth2 PKCE Flow

### 3.1 Create OauthService

```typescript
// core/service/oauth-service.ts
@Injectable({ providedIn: 'root' })
export class OauthService {
  private clientId = 'my-new-client';
  private redirectUri = `${window.location.origin}/<app-path>/login/callback`;
  private authServer = 'http://shopbuddy.com/auth';

  // PKCE helpers
  private generateCodeVerifier(length = 128): string { /* random hex */ }
  private async generateCodeChallenge(verifier: string): Promise<string> { /* SHA-256 + base64url */ }

  async login() {
    const verifier = this.generateCodeVerifier();
    sessionStorage.setItem('pkce_verifier', verifier);
    const challenge = await this.generateCodeChallenge(verifier);
    window.location.href = `${this.authServer}/oauth2/authorize?...&code_challenge=${challenge}&code_challenge_method=S256`;
  }

  exchangeCodeForToken(code: string) {
    const body = new URLSearchParams();
    body.set('grant_type', 'authorization_code');
    body.set('code', code);
    body.set('redirect_uri', this.redirectUri);
    body.set('client_id', this.clientId);
    body.set('code_verifier', sessionStorage.getItem('pkce_verifier')!);
    return this.http.post<any>(`${this.authServer}/oauth2/token`, body.toString(), {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
    });
  }

  storeTokens(tokens: any) { /* save to sessionStorage */ }
  getAccessToken(): string | null { return sessionStorage.getItem('access_token'); }
  isLoggedIn(): boolean { /* check token + expiry */ }
  logout(): void { /* clear sessionStorage */ }
}
```

### 3.2 Create Auth Guard

```typescript
// core/guard/auth-guard.ts
export const authGuard: CanActivateFn = (route, state) => {
  const auth = inject(OauthService);
  const router = inject(Router);
  return auth.isLoggedIn() ? true : router.navigate(['/login']) && false;
};
```

### 3.3 Create Auth Interceptor

Attaches the `Authorization: Bearer` header to all outgoing HTTP requests:

```typescript
// core/interceptor/auth-interceptor.ts
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = inject(OauthService).getAccessToken();
  return token
    ? next(req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }))
    : next(req);
};
```

### 3.4 Register the interceptor

```typescript
// app.config.ts
provideHttpClient(withFetch(), withInterceptors([authInterceptor]))
```

### 3.5 Add routes

```typescript
export const routes: Routes = [
  { path: 'login', component: Login },
  { path: 'login/callback', component: LoginCallback },
  { path: '', redirectTo: 'overview', pathMatch: 'full' },
  { path: 'overview', component: Overview, canActivate: [authGuard] },
  // ... other guarded routes
];
```

### 3.6 Conditionally show layout

In the root component, only show the sidebar/nav when logged in:

```html
@if (auth.isLoggedIn()) {
  <app-sidebar />
  <router-outlet />
} @else {
  <router-outlet />
}
```

---

## Troubleshooting

### 400 Bad Request on `/oauth2/authorize`

**Symptom:**
```
GET http://shopbuddy.com/auth/oauth2/authorize?client_id=k8s-dashboard-client
    &redirect_uri=http%3A%2F%2Flocalhost%3A4300%2F...
    400 (Bad Request)
```

**Cause:** The `redirect_uri` sent by the frontend does not exactly match any URI registered for that client in `application.yml`. The URI must match on scheme, host, port, and path — even a port difference (e.g., `4200` vs `4300`) will cause a 400.

**Fix:** Add the actual port to the client's `redirect-uris`:

```yaml
redirect-uris:
  - http://shopbuddy.com/k8s-dashboard/login/callback
  - http://localhost:4200/k8s-dashboard/login/callback
  - http://localhost:4300/k8s-dashboard/login/callback
```

Redeploy the auth-server after the change (Skaffold will detect it automatically).

**Prevention:** `ng serve` defaults to port 4200, but increments to 4201, 4300, etc. if the port is already taken (e.g., another Angular app is running). Register all ports you expect to use upfront.

---

## Verification Checklist

1. Navigate to the app URL — should redirect to `/login`
2. Click "Sign in" — should redirect to auth-server login page
3. After login + consent — should redirect back to `/login/callback`, then to `/`
4. Network tab should show `Authorization: Bearer <jwt>` on API calls
5. Accessing the backend API directly without a token should return `401`
6. Clicking logout should clear tokens and redirect to `/login`
