package dev.agasen.api.user;

import dev.agasen.api.user.permission.PermissionCreationDetails;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange( "/permissions" )
public interface PermissionService {

   @PostExchange
   void createPermission( @RequestBody PermissionCreationDetails permissionCreationDetails );
}
