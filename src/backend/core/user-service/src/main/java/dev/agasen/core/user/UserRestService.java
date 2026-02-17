package dev.agasen.core.user;

import dev.agasen.api.user.UserService;
import dev.agasen.api.user.user.*;
import dev.agasen.common.exceptions.Exceptions;
import dev.agasen.core.user.mapper.UserMapper;
import dev.agasen.core.user.persistence.RoleRepository;
import dev.agasen.core.user.persistence.UserRepository;
import dev.agasen.core.user.persistence.entity.Role;
import dev.agasen.core.user.persistence.entity.User;
import dev.agasen.core.user.persistence.entity.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequiredArgsConstructor
@Slf4j
public class UserRestService implements UserService {

   private final UserRepository userRepository;
   private final RoleRepository roleRepository;
   private final UserMapper userMapper;
   private final PasswordEncoder passwordEncoder;

   @Override
   @Transactional( readOnly = true )
   public Page< UserDetails > getUsers( int page, int size, List< String > roles, List< String > permissions ) {
      return userRepository.findFilteredUsers( roles, permissions, PageRequest.of( page, size ) )
            .map( userMapper::toUserDetails );
   }

   @Override
   @Transactional( readOnly = true )
   public UserDetails getUser( long id ) {
      return userRepository.findById( id )
            .map( userMapper::toUserDetails )
            .orElseThrow( Exceptions.notFound( "User", id ) );
   }

   @Override
   @Transactional
   public void createUser( UserCreationDetails userCreationDetails ) {
      User user = userMapper.toUser( userCreationDetails );
      user.setPassword( passwordEncoder.encode( user.getPassword() ) );
      Collection< Role > roles = roleRepository.findAllExistByNameIn( userCreationDetails.getRoles() );

      var userRoles = roles.stream()
            .map( r -> new UserRole( null, user, r ) )
            .collect( Collectors.toSet() );
      user.setUserRoles( userRoles );

      userRepository.save( user );
   }

   @Override
   @Transactional
   public void updateUserPassword( long id, UserPasswordChange userPasswordChange ) {
      User user = userRepository.findById( id )
            .orElseThrow( Exceptions.notFound( "User", id ) );

      if ( passwordEncoder.matches( userPasswordChange.getCurrentPassword(), user.getPassword() ) ) {
         user.setPassword( passwordEncoder.encode( userPasswordChange.getNewPassword() ) );
         log.debug( "Password changed for user with id {}", id );
      } else {
         throw new RuntimeException( "Current password did not match" );
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
      throw new UnsupportedOperationException( "not yet supported" );
   }
}
