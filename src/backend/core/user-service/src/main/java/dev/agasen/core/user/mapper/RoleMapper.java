package dev.agasen.core.user.mapper;

import dev.agasen.api.user.permission.PermissionDetails;
import dev.agasen.api.user.role.RoleCreationDetails;
import dev.agasen.api.user.role.RoleDetails;
import dev.agasen.core.user.persistence.entity.Role;
import dev.agasen.core.user.persistence.entity.RolePermission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(
      componentModel = "spring",
      uses = { PermissionMapper.class },
      unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface RoleMapper {

   RoleMapper INSTANCE = Mappers.getMapper( RoleMapper.class );

   @Mapping( source = "rolePermissions", target = "permissions", qualifiedByName = "rolePermissionsToPermissions" )
   RoleDetails roleToRoleDetails( Role role );

   List< RoleDetails > rolesToRoleDetails( List< Role > roles );

   @Mapping( target = "id", ignore = true )
   @Mapping( target = "rolePermissions", ignore = true )
   @Mapping( target = "userRoles", ignore = true )
   Role toRole( RoleCreationDetails roleCreationDetails );

   @Named( "rolePermissionsToPermissions" )
   static Set< PermissionDetails > rolePermissionsToPermissions( Set< RolePermission > rolePermissions ) {
      if ( rolePermissions == null ) {
         return new HashSet<>();
      }
      return rolePermissions.stream()
            .map( RolePermission::getPermission )
            .map( PermissionMapper.INSTANCE::permissionToPermissionDetails )
            .collect( Collectors.toSet() );
   }
}