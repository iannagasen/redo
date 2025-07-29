package dev.agasen.core.user;

import dev.agasen.core.user.domain.RoleCreationDetails;
import dev.agasen.core.user.domain.RoleDetails;
import dev.agasen.core.user.domain.RoleModificationDetails;
import dev.agasen.core.user.domain.RoleRemovalDetails;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface RoleManagementSpec {

   @GetMapping( "/roles" )
   List< RoleDetails > getRoles();

   @PostMapping( "/roles" )
   void createRole(
         @RequestBody @Valid RoleCreationDetails roleCreationDetails
   );

   @PutMapping( "/roles" )
   void updateRole(
         @RequestBody RoleModificationDetails roleModificationDetails
   );

   @DeleteMapping( "/roles/{id}" )
   void deleteRole(
         @PathVariable long id,
         @RequestBody RoleRemovalDetails roleRemovalDetails
   );
}
