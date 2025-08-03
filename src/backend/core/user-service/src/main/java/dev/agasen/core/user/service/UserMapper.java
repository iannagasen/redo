package dev.agasen.core.user.service;

import dev.agasen.api.user.role.RoleDetails;
import dev.agasen.api.user.user.UserCreationDetails;
import dev.agasen.api.user.user.UserDetails;
import dev.agasen.core.user.persistence.entity.User;
import dev.agasen.core.user.persistence.entity.UserRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(
      componentModel = "spring",
      uses = { RoleMapper.class },
      unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface UserMapper {

   UserMapper INSTANCE = Mappers.getMapper( UserMapper.class );

   @Mapping( target = "roles", source = "userRoles", qualifiedByName = "userRolesToRoles" )
   UserDetails toUserDetails( User user );

   @Mapping( target = "id", ignore = true )
   @Mapping( target = "userRoles", ignore = true ) // handled in logic
   @Mapping( target = "locked", ignore = true )
   @Mapping( target = "enabled", ignore = true )
   @Mapping( target = "deleted", ignore = true )
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
