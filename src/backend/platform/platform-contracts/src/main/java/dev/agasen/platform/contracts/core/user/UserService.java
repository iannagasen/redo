package dev.agasen.platform.contracts.core.user;

import dev.agasen.platform.contracts.core.user.user.UserCreationDetails;
import dev.agasen.platform.contracts.core.user.user.UserDetails;
import dev.agasen.platform.contracts.core.user.user.UserModificationDetails;
import dev.agasen.platform.contracts.core.user.user.UserPasswordChange;
import dev.agasen.platform.contracts.core.user.user.UserRoleAssignmentDetails;
import dev.agasen.platform.contracts.core.user.user.UserRolesDetails;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PatchExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

import java.util.List;

@HttpExchange( "/users" )
public interface UserService {

   @GetExchange
   Page< UserDetails > getUsers(
      @RequestParam( defaultValue = "0", name = "page" ) int page,
      @RequestParam( defaultValue = "1", name = "size" ) int size,
      @RequestParam( name = "roles", required = false ) List< String > roles,
      @RequestParam( name = "permissions", required = false ) List< String > permissions
   );

   @GetExchange( "/{id}" )
   UserDetails getUser( @PathVariable( "id" ) long id );

   @PostExchange
   void createUser( @RequestBody UserCreationDetails userCreationDetails );

   @PostExchange( "/{id}/password" )
   void updateUserPassword(
      @PathVariable( "id" ) long id,
      @RequestBody UserPasswordChange userPasswordChange
   );

   @PutExchange
   void updateUser( @RequestBody UserModificationDetails userModificationDetails );

   @DeleteExchange( "/{id}" )
   void deleteUser( @PathVariable( "id" ) long id );

   @PatchExchange( "/{id}/enable" )
   void enableUser( @PathVariable( "id" ) long id );

   @PostExchange( "/{id}/roles" )
   void addRole( @PathVariable( "id" ) long id, @RequestBody UserRoleAssignmentDetails userRoleAssignmentDetails );

   @GetExchange( "/{id}/roles" )
   List< UserRolesDetails > getUserRoles( @PathVariable( "id" ) long id );
}
