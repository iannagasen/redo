# Payment Service — Lessons Learned

A record of non-obvious failures encountered while building the payment service and Kafka
integration, what caused them, and how they were resolved.

---

## 1. ERR_TOO_MANY_REDIRECTS — Missing SecurityConfig

### Symptom

```
GET http://shopbuddy.com/payment/login;jsessionid=B1B2B037... net::ERR_TOO_MANY_REDIRECTS
```

The browser was stuck in a redirect loop: `/payment/api/v1/payments` → `302 → /payment/login`
→ `302 → /payment/login` → repeat.

### Root Cause

Spring Boot's default security configuration (when no `SecurityFilterChain` beans are present)
enables **form-based login with HTTP sessions**. When an unauthenticated request arrives, it
redirects to `/login`. Since there is no login page at `/payment/login`, the redirect loops.

This happened because `ResourceServerSecurityConfiguration` was originally an `@AutoConfiguration`
in `platform:common`, but at some point the autoconfiguration imports file was deleted
(`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`) and each
service was given its own `SecurityConfig.java`. The payment service was new and never received one.

### Fix

Create `SecurityConfig.java` in `dev.agasen.core.payment.config` following the same pattern as
the other services:

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean @Order(1)
    public SecurityFilterChain publicFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/actuator/**", "/swagger-ui/**", ...)
            .sessionManagement(s -> s.sessionCreationPolicy(STATELESS))
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(a -> a.anyRequest().permitAll())
            .oauth2ResourceServer(AbstractHttpConfigurer::disable);
        return http.build();
    }

    @Bean @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(STATELESS))
            .authorizeHttpRequests(a -> a.anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwkSetUri(...)));
        return http.build();
    }
}
```

### Rule

> Every Spring Boot service must have an explicit `SecurityConfig`.
> If `@EnableWebSecurity` is on the classpath and no `SecurityFilterChain` bean is defined,
> Spring Boot auto-configures form login — which is inappropriate for a stateless REST API.

---

## 2. CORS — Redirect to /login Instead of 401

### Symptom

```
Access to fetch at 'http://shopbuddy.com/payment/api/v1/payments' from origin 'http://localhost:4200'
has been blocked by CORS policy: Response to preflight request doesn't pass access control check:
No 'Access-Control-Allow-Origin' header is present on the requested resource.
```

### Root Cause

There were actually two separate issues hiding behind the same CORS error message:

**Issue A — Service not deployed.**
When no pod is running, the nginx ingress returns a 503 to the OPTIONS preflight. A 503 with no
CORS headers looks identical to a CORS rejection in the browser console.

**Issue B — Missing SecurityConfig (see failure #1).**
Even after the service was running, the Spring Security form-login redirect (302) on the OPTIONS
preflight also surfaces as a CORS error because the redirect response has no CORS headers.

### Fix

Two parts:

1. Add CORS annotations to the k8s Ingress. The nginx ingress controller handles OPTIONS
   preflights itself and adds CORS headers — even if the pod is temporarily unavailable:

   ```yaml
   annotations:
     nginx.ingress.kubernetes.io/enable-cors: "true"
     nginx.ingress.kubernetes.io/cors-allow-origin: "http://localhost:4200, http://shopbuddy.com"
     nginx.ingress.kubernetes.io/cors-allow-methods: "GET, POST, PUT, DELETE, OPTIONS"
     nginx.ingress.kubernetes.io/cors-allow-headers: "Content-Type, Authorization"
   ```

2. Fix the SecurityConfig (failure #1 above) so the service returns 401 instead of 302.

### Rule

> A CORS error in the browser does not always mean the CORS configuration is wrong.
> It can be caused by a 3xx redirect, a 5xx error, or the server being unreachable — any
> response without `Access-Control-Allow-Origin` on a preflight will be reported as CORS.
> Check the actual HTTP response code, not just the CORS error message.

---

## 3. Angular Zoneless — Form Submission Did Nothing

### Symptom

Clicking the Pay button produced no visible effect. No network request, no error, no state change.

### Root Cause

The checkout component used Angular's **template-driven forms** (`FormsModule`, `(ngSubmit)`,
`[(ngModel)]`). These APIs rely on zone.js to schedule change detection after DOM events.

ShopBuddy's frontend uses **zoneless change detection** (Angular 19 signals-based). Without
zone.js, Angular's `NgForm` directive intercepted the native `submit` event but the `(ngSubmit)`
output was emitted into a zone that never triggered a CD cycle — so `submitPayment()` was never
called from the user's perspective (no side effects ran).

### Fix

Replace `(ngSubmit)` with a plain `(click)` on a `type="button"`. Angular's event binding
dispatches clicks synchronously without zone involvement:

```html
<!-- Before — silently dead in zoneless apps -->
<form (ngSubmit)="submitPayment()">
  <button type="submit">Pay</button>
</form>

<!-- After — works in zoneless -->
<button type="button" (click)="submitPayment()">Pay</button>
```

### Rule

> In a zoneless Angular app, avoid `FormsModule` submit flow and `NgForm`-based submit handlers.
> Use `(click)` on `type="button"` elements and read form values directly.

---

## 4. Angular Zoneless — [(ngModel)] Did Not Update Component Properties

### Symptom

After switching to `(click)`, the Pay button now called `submitPayment()`, but validation
always failed with "Please fill in all card details." even when all fields were visibly filled in.

### Root Cause

`[(ngModel)]` two-way binding also depends on zone.js. The `NgModel` directive listens to the
input's `change` event and calls `markForCheck()` via the `ChangeDetectorRef`. In a zoneless app
this does not trigger re-evaluation, so the write-back from DOM → component property never happened.
The component properties stayed at their initial empty values.

### Fix (Attempt 1 — Partial)

Replace `[(ngModel)]` with `(input)` event bindings that assign directly to plain properties:

```html
<input type="text" (input)="cardholderName = $any($event.target).value" />
```

This fixed the binding but was fragile — plain class properties are not reactive in zoneless mode.

### Fix (Attempt 2 — Correct)

Convert all form field state to **signals**. Signals are the proper reactive primitive in
zoneless Angular — their `.set()` is synchronous and guaranteed to update the value regardless
of the CD strategy:

```typescript
// Component class
cardholderName = signal('');
cardNumber     = signal('');
expiryMonth    = signal(0);
expiryYear     = signal(0);
cvv            = signal('');
```

```html
<!-- Template -->
<input type="text" (input)="cardholderName.set($any($event.target).value)" />
```

```typescript
// In submitPayment()
if (!this.cardholderName() || !this.cardNumber() ...) { ... }
```

### Rule

> In a zoneless Angular 19 app, **all mutable component state should be signals**.
> Use `(input)` + `signal.set()` for form fields instead of `[(ngModel)]`.
> `FormsModule` and `ReactiveFormsModule` both have zone.js dependencies that make them
> unreliable in zoneless mode.

---

## 5. Kafka UI — Static Assets 404 Under Sub-Path

### Symptom

Navigating to `shopbuddy.com/kafka-ui` loaded the page shell but all JS/CSS assets 404'd:

```
GET http://shopbuddy.com/assets/index-77be7e32.js    404 Not Found
GET http://shopbuddy.com/assets/index-d9d582cd.css   404 Not Found
```

### Root Cause

The initial ingress used an nginx `rewrite-target` to strip `/kafka-ui` before forwarding to
the pod:

```yaml
annotations:
  nginx.ingress.kubernetes.io/rewrite-target: /$2
path: /kafka-ui(/|$)(.*)
```

This meant the Kafka UI app received requests at `/` and thought it was the root app. Its HTML
then rendered `<script src="/assets/index.js">` (root-relative). The browser requested
`shopbuddy.com/assets/index.js` — no ingress rule existed for `/assets` → 404.

### Fix

Tell the Kafka UI application that it is served under `/kafka-ui` via an environment variable.
The app then generates `/kafka-ui/assets/index.js` in its HTML, which the ingress correctly
routes back to the pod. No path rewriting needed:

```yaml
# Deployment env var
- name: SERVER_SERVLET_CONTEXT_PATH
  value: "/kafka-ui"
```

```yaml
# Ingress — simple prefix, no rewrite
- pathType: Prefix
  path: "/kafka-ui"
```

### Rule

> When hosting a UI under a sub-path in k8s, prefer setting the app's **base/context path**
> via its own configuration rather than rewriting paths at the ingress.
> Path rewriting breaks apps that generate absolute asset URLs based on where they think they
> are serving from.

---

## 6. k8s Ingress — Regex Path Rejected with pathType: Prefix

### Symptom

```
Error from server (BadRequest): admission webhook "validate.nginx.ingress.kubernetes.io" denied
the request: ingress contains invalid paths: path /kafka-ui(/|$)(.*) cannot be used with
pathType Prefix
```

### Root Cause

The nginx ingress admission webhook enforces that `pathType: Prefix` only accepts literal path
prefixes. Regex patterns (containing `(`, `)`, `|`, `.*`) require `pathType: ImplementationSpecific`.

### Fix

```yaml
# Before
- pathType: Prefix
  path: "/kafka-ui(/|$)(.*)"

# After
- pathType: ImplementationSpecific
  path: "/kafka-ui(/|$)(.*)"
```

### Rule

> Use `pathType: ImplementationSpecific` whenever the path contains regex characters.
> Use `pathType: Prefix` for plain literal paths like `/payment` or `/order`.
