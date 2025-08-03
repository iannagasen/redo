package dev.agasen.core.user;

import dev.agasen.api.user.UserRestControllerSpec;
import dev.agasen.api.user.user.*;
import dev.agasen.common.utility.Exceptions;
import dev.agasen.core.user.persistence.RoleRepository;
import dev.agasen.core.user.persistence.UserRepository;
import dev.agasen.core.user.persistence.entity.Role;
import dev.agasen.core.user.persistence.entity.User;
import dev.agasen.core.user.persistence.entity.UserRole;
import dev.agasen.core.user.service.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequiredArgsConstructor
@Slf4j
public class UserRestController implements UserRestControllerSpec {

   private final UserRepository userRepository;
   private final RoleRepository roleRepository;
   private final UserMapper userMapper;

   @Override
   public Page< UserDetails > getUsers( int page, int size, List< String > roles, List< String > permissions ) {
      return userRepository.findFilteredUsers( roles, permissions, PageRequest.of( page, size ) )
            .map( userMapper::toUserDetails );
   }

   @Override
   public UserDetails getUser( long id ) {
      return userRepository.findById( id )
            .map( userMapper::toUserDetails )
            .orElseThrow( Exceptions.notFound( "User", id ) );
   }

   @Override
   public void createUser( UserCreationDetails userCreationDetails ) {
      User user = userMapper.toUser( userCreationDetails );
      Collection< Role > roles = roleRepository.findAllExistByNameIn( userCreationDetails.getRoles() );

      var userRoles = roles.stream()
            .map( r -> new UserRole( null, user, r ) )
            .collect( Collectors.toSet() );
      user.setUserRoles( userRoles );

      userRepository.save( user );
   }

   @Override
   public void updateUserPassword( long id, UserPasswordChange userPasswordChange ) {
      User user = userRepository.findById( id )
            .orElseThrow( Exceptions.notFound( "User", id ) );

      // TODO: use encoding , hashes , etc
      if ( user.getPassword().equals( userPasswordChange.getCurrentPassword() ) ) {
         user.setPassword( userPasswordChange.getCurrentPassword() );
         log.debug( "Password changed for user with id {}", id );
      } else {
         throw new RuntimeException( "password did not match" );
      }
   }

   @Override
   public void updateUser( UserModificationDetails userModificationDetails ) {

   }

   @Override
   public void deleteUser( long id ) {
      userRepository.findById( id )
            .orElseThrow( Exceptions.notFound( "User", id ) )
            .setDeleted( true );
   }

   @Override
   @Transactional
   public void enableUser( long id ) {
      userRepository.findById( id )
            .orElseThrow( Exceptions.notFound( "User", id ) )
            .setEnabled( true );
   }

   @Override
   public void addRole( long id, UserRoleAssignmentDetails userRoleAssignmentDetails ) {
      userRepository.findById( id )
            .orElseThrow( Exceptions.notFound( "User", id ) )
            .addRoles( roleRepository.findAllExistByNameIn( userRoleAssignmentDetails.getRoles() ) );
   }

   @Override
   public List< UserRolesDetails > getUserRoles( long id ) {
      return List.of();
   }
}
