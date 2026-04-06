package dev.agasen.core.user.application.mapper;


import dev.agasen.api.core.user.permission.PermissionCreationDetails;
import dev.agasen.api.core.user.permission.PermissionDetails;
import dev.agasen.core.user.persistence.entity.Permission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.Set;

@Mapper(
   componentModel = "spring",
   unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface PermissionMapper {

   PermissionMapper INSTANCE = Mappers.getMapper( PermissionMapper.class );

   PermissionDetails permissionToPermissionDetails( Permission permission );

   @Mapping( target = "id", ignore = true )
   Permission toPermission( PermissionCreationDetails permissionCreationDetails );

   @Mapping( target = "id", ignore = true )
   Permission permissionDTOToPermission( PermissionDetails permissionDTO );

   Set< Permission > permissionDTOsToPermissions( Set< PermissionDetails > permissionDTOs );
}