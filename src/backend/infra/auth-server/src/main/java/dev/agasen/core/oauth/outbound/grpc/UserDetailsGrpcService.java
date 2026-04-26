package dev.agasen.core.oauth.outbound.grpc;

import dev.agasen.platform.contracts.api.grpc.user.UserAuthInfo;
import dev.agasen.platform.contracts.api.grpc.user.UserAuthInfoRequest;
import dev.agasen.platform.contracts.api.grpc.user.UserAuthServiceGrpc;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty( name = "userdetails.transport", havingValue = "grpc" )
public class UserDetailsGrpcService implements UserDetailsService {

   private final UserAuthServiceGrpc.UserAuthServiceBlockingStub userAuthServiceBlockingStub;

   @Override
   public UserDetails loadUserByUsername( String username ) throws UsernameNotFoundException {
      log.info( "Fetching user info for '{}' via gRPC", username );
      try {
         UserAuthInfo authInfo = userAuthServiceBlockingStub
            .withDeadlineAfter( 5, TimeUnit.SECONDS )  // fresh 5s countdown per call
            .getUserAuthInfo( UserAuthInfoRequest.newBuilder().setUsername( username ).build() );

         List< GrantedAuthority > authorities = new ArrayList<>();
         authInfo.getRolesList().forEach( role -> authorities.add( new SimpleGrantedAuthority( "ROLE_" + role ) ) );
         authInfo.getPermissionsList().forEach( perm -> authorities.add( new SimpleGrantedAuthority( perm ) ) );

         return User.builder()
            .username( authInfo.getUsername() )
            .password( authInfo.getPasswordHash() )
            .authorities( authorities )
            .disabled( !authInfo.getEnabled() )
            .accountLocked( authInfo.getLocked() )
            .accountExpired( authInfo.getDeleted() )
            .build();

      } catch ( StatusRuntimeException e ) {
         log.error( "gRPC error fetching user info for '{}': {}", username, e.getStatus() );
         throw new UsernameNotFoundException( "User not found: " + username, e );
      }
   }
}
