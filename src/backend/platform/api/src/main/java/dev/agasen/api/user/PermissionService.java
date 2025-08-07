package dev.agasen.api.user;

import dev.agasen.api.user.permission.PermissionCreationDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface PermissionService {

   String ENDPOINT = "/permissions";

   @PostMapping( ENDPOINT )
   void createPermission(
         @RequestBody PermissionCreationDetails permissionCreationDetails
   );

}
