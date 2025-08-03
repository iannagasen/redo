package dev.agasen.core.user.service;


import dev.agasen.api.user.permission.PermissionDetails;
import dev.agasen.core.user.persistence.entity.Permission;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Set;

@Mapper( componentModel = "spring" )
public interface PermissionMapper {

   PermissionMapper INSTANCE = Mappers.getMapper( PermissionMapper.class );

   PermissionDetails permissionToPermissionDetails( Permission permission );

   Permission permissionDTOToPermission( PermissionDetails permissionDTO );

   List< PermissionDetails > permissionsToPermissionDetails( List< Permission > permissions );

   List< Permission > permissionDTOsToPermissions( List< PermissionDetails > permissionDTOs );

   Set< PermissionDetails > permissionsToPermissionDetails( Set< Permission > permissions );

   Set< Permission > permissionDTOsToPermissions( Set< PermissionDetails > permissionDTOs );
}