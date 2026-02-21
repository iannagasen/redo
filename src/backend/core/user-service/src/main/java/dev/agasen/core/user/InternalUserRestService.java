package dev.agasen.core.user;

import dev.agasen.api.user.InternalUserService;
import dev.agasen.api.user.user.UserAuthInfo;
import dev.agasen.common.exceptions.Exceptions;
import dev.agasen.core.user.persistence.UserRepository;
import dev.agasen.core.user.persistence.entity.Permission;
import dev.agasen.core.user.persistence.entity.Role;
import dev.agasen.core.user.persistence.entity.RolePermission;
import dev.agasen.core.user.persistence.entity.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class InternalUserRestService implements InternalUserService {

   private final UserRepository userRepository;

   @Override
   @Transactional( readOnly = true )
   public UserAuthInfo getAuthInfo( String username ) {
      return userRepository.findByUsername( username )
         .map( user -> {
            Set< Role > roles = user.getUserRoles().stream()
               .map( UserRole::getRole )
               .collect( Collectors.toSet() );

            Set< String > roleNames = roles.stream()
               .map( Role::getName )
               .collect( Collectors.toSet() );

            Set< String > permissionNames = roles.stream()
               .flatMap( role -> role.getRolePermissions().stream() )
               .map( RolePermission::getPermission )
               .map( Permission::getName )
               .collect( Collectors.toSet() );

            return UserAuthInfo.builder()
               .id( user.getId() )
               .username( user.getUsername() )
               .password( user.getPassword() )
               .enabled( user.isEnabled() )
               .locked( user.isLocked() )
               .deleted( user.isDeleted() )
               .roles( roleNames )
               .permissions( permissionNames )
               .build();
         } )
         .orElseThrow( Exceptions.notFound( "User not found: " + username ) );
   }
}
