package dev.agasen.core.user.service;

import dev.agasen.api.user.role.RoleDetails;
import dev.agasen.api.user.user.UserCreationDetails;
import dev.agasen.api.user.user.UserDetails;
import dev.agasen.core.user.persistence.entity.User;
import dev.agasen.core.user.persistence.entity.UserRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper( componentModel = "spring", uses = { RoleMapper.class } )
public interface UserMapper {

   UserMapper INSTANCE = Mappers.getMapper( UserMapper.class );

   @Mapping( target = "password", ignore = true )
   @Mapping( source = "userRoles", target = "roles", qualifiedByName = "userRolesToRoles" )
   UserDetails toUserDetails( User user );

   User toUser( UserCreationDetails userCreationDetails );

   @Named( "userRolesToRoles" )
   default Set< RoleDetails > userRolesToRoles( Set< UserRole > userRoles ) {
      if ( userRoles == null ) return new HashSet<>();

      return userRoles.stream()
            .map( UserRole::getRole )
            .map( RoleMapper.INSTANCE::roleToRoleDetails )
            .collect( Collectors.toSet() );
   }

}
