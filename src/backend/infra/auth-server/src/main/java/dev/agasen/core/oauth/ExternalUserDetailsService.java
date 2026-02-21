package dev.agasen.core.oauth;

import dev.agasen.api.user.user.UserAuthInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalUserDetailsService implements UserDetailsService {

    private final RestTemplate restTemplate;

    @Value("${env.base.url.internal.user}")
    private String userServiceUrl;

    @Value("${internal.api.key}")
    private String internalApiKey;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Fetching user info for '{}' from user-service", username);
        
        String url = userServiceUrl + "/internal/users/" + username + "/auth-info";
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Internal-Api-Key", internalApiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            UserAuthInfo authInfo = restTemplate.exchange(url, HttpMethod.GET, entity, UserAuthInfo.class).getBody();
            
            if (authInfo == null) {

            throw new UsernameNotFoundException( "User not found: " + username );
         }

         List< GrantedAuthority > authorities = new ArrayList<>();
         authInfo.getRoles().forEach( role -> authorities.add( new SimpleGrantedAuthority( "ROLE_" + role ) ) );
         authInfo.getPermissions().forEach( perm -> authorities.add( new SimpleGrantedAuthority( perm ) ) );

         return User.builder()
            .username( authInfo.getUsername() )
            .password( authInfo.getPassword() )
            .authorities( authorities )
            .disabled( !authInfo.isEnabled() )
            .accountLocked( authInfo.isLocked() )
            .accountExpired( authInfo.isDeleted() )
            .build();

      } catch ( Exception e ) {
         log.error( "Error fetching user info for '{}'", username, e );
         throw new UsernameNotFoundException( "Error fetching user info for: " + username, e );
      }
   }
}
