package dev.agasen.core.user;

import dev.agasen.api.user.PermissionService;
import dev.agasen.api.user.permission.PermissionCreationDetails;
import dev.agasen.core.user.mapper.PermissionMapper;
import dev.agasen.core.user.persistence.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PermissionRestService implements PermissionService {

   private final PermissionRepository permissionRepository;
   private final PermissionMapper permissionMapper;

   @Override
   public void createPermission( PermissionCreationDetails permissionCreationDetails ) {
      var permission = permissionMapper.toPermission( permissionCreationDetails );
      permissionRepository.save( permission );
   }
}
