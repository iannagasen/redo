package dev.agasen.core.user;

import dev.agasen.api.user.UserRestControllerSpec;
import dev.agasen.api.user.user.*;
import dev.agasen.core.user.persistence.RoleRepository;
import dev.agasen.core.user.persistence.UserRepository;
import dev.agasen.core.user.persistence.entity.Role;
import dev.agasen.core.user.persistence.entity.User;
import dev.agasen.core.user.persistence.entity.UserRole;
import dev.agasen.core.user.service.UserMapper;
import dev.agasen.core.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class UserRestController implements UserRestControllerSpec {

   private final UserRepository userRepository;
   private final RoleRepository roleRepository;
   private final UserMapper userMapper;
   private final UserService userService;

   @Override
   public Page< UserDetails > getUsers( int page, int size, List< String > roles, List< String > permissions ) {
      return userRepository.findFilteredUsers( roles, permissions, PageRequest.of( page, size ) )
            .map( userMapper::toUserDetails );
   }

   @Override
   public UserDetails getUser( long id ) {
      return null;
   }

   @Override
   public void createUser( UserCreationDetails userCreationDetails ) {
      User user = userMapper.toUser( userCreationDetails );
      Collection< Role > roles = roleRepository.findByNameIn( userCreationDetails.getRoles() );

      boolean hasMissing = userCreationDetails.getRoles().size() != roles.size();
      if ( hasMissing ) {
         var found = roles.stream().map( Role::getName ).collect( Collectors.toSet() );
         var missing = new HashSet<>( userCreationDetails.getRoles() );
         missing.removeAll( found );
         throw new IllegalArgumentException( "Missing roles: " + missing );
      }

      var userRoles = roles.stream()
            .map( r -> new UserRole( null, user, r ) )
            .collect( Collectors.toSet() );
      user.setUserRoles( userRoles );

      userRepository.save( user );
   }

   @Override
   public void updateUser( UserModificationDetails userModificationDetails ) {

   }

   @Override
   public void deleteUser( long id ) {

   }

   @Override
   public void enableUser( long id ) {

   }

   @Override
   public void addRole( long id, UserRoleAssignmentDetails userRoleAssignmentDetails ) {

   }

   @Override
   public List< UserRolesDetails > getUserRoles( long id ) {
      return List.of();
   }
}
