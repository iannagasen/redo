package dev.agasen.api.user;

import dev.agasen.api.user.role.RoleCreationDetails;
import dev.agasen.api.user.role.RoleDetails;
import dev.agasen.api.user.role.RoleModificationDetails;
import dev.agasen.api.user.role.RoleRemovalDetails;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface RoleService {

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
