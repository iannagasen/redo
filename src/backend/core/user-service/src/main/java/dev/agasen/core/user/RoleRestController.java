package dev.agasen.core.user;

import dev.agasen.api.user.RoleRestControllerSpec;
import dev.agasen.api.user.role.RoleCreationDetails;
import dev.agasen.api.user.role.RoleDetails;
import dev.agasen.api.user.role.RoleModificationDetails;
import dev.agasen.api.user.role.RoleRemovalDetails;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RoleRestController implements RoleRestControllerSpec {

   @Override
   public List< RoleDetails > getRoles() {
      return List.of();
   }

   @Override
   public void createRole( RoleCreationDetails roleCreationDetails ) {

   }

   @Override
   public void updateRole( RoleModificationDetails roleModificationDetails ) {

   }

   @Override
   public void deleteRole( long id, RoleRemovalDetails roleRemovalDetails ) {

   }
}
