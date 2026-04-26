package dev.agasen.platform.contracts.core.user;

import dev.agasen.platform.contracts.core.user.role.RoleCreationDetails;
import dev.agasen.platform.contracts.core.user.role.RoleDetails;
import dev.agasen.platform.contracts.core.user.role.RoleModificationDetails;
import dev.agasen.platform.contracts.core.user.role.RoleRemovalDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

import java.util.List;

@HttpExchange( "/roles" )
public interface RoleService {

   @GetExchange
   List< RoleDetails > getRoles();

   @PostExchange
   void createRole( @RequestBody RoleCreationDetails roleCreationDetails );

   @PutExchange
   void updateRole( @RequestBody RoleModificationDetails roleModificationDetails );

   @DeleteExchange( "/{id}" )
   void deleteRole( @PathVariable long id, @RequestBody RoleRemovalDetails roleRemovalDetails );
}
