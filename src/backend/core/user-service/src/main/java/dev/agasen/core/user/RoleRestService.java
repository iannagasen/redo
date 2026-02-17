package dev.agasen.core.user;

import dev.agasen.api.user.RoleService;
import dev.agasen.api.user.role.RoleCreationDetails;
import dev.agasen.api.user.role.RoleDetails;
import dev.agasen.api.user.role.RoleModificationDetails;
import dev.agasen.api.user.role.RoleRemovalDetails;
import dev.agasen.core.user.mapper.RoleMapper;
import dev.agasen.core.user.persistence.PermissionRepository;
import dev.agasen.core.user.persistence.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RoleRestService implements RoleService {

   private final RoleRepository roleRepository;
   private final PermissionRepository permissionRepository;
   private final RoleMapper roleMapper;

   @Override
   public List< RoleDetails > getRoles() {
      return List.of();
   }

   @Override
   public void createRole( RoleCreationDetails roleCreationDetails ) {
      var role = roleMapper.toRole( roleCreationDetails );
      var permissions = permissionRepository.findAllExistsByNameIn( roleCreationDetails.getPermissions() );
      role.addPermissions( permissions );
      roleRepository.save( role );
   }

   @Override
   public void updateRole( RoleModificationDetails roleModificationDetails ) {

   }

   @Override
   public void deleteRole( long id, RoleRemovalDetails roleRemovalDetails ) {

   }
}
