package dev.agasen.api.user;

import dev.agasen.api.user.user.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface UserService {

   @GetMapping( "/users" )
   Page< UserDetails > getUsers(
         @RequestParam( defaultValue = "0", name = "page" ) @Min( 0 ) int page,
         @RequestParam( defaultValue = "1", name = "size" ) @Min( 1 ) @Max( 100 ) int size,
         @RequestParam( name = "roles", required = false ) List< String > roles,
         @RequestParam( name = "permissions", required = false ) List< String > permissions
   );

   @GetMapping( "/users/{id}" )
   UserDetails getUser(
         @PathVariable( name = "id" ) long id
   );

   @PostMapping( "/users" )
   void createUser(
         @RequestBody UserCreationDetails userCreationDetails
   );

   @PostMapping( "/users/{id}/password" )
   void updateUserPassword(
         @PathVariable( name = "id" ) long id,
         @RequestBody UserPasswordChange userPasswordChange
   );

   @PutMapping( "/users" )
   void updateUser(
         @RequestBody UserModificationDetails userModificationDetails
   );

   @DeleteMapping( "/users/{id}" )
   void deleteUser(
         @PathVariable( name = "id" ) long id
   );

   @PatchMapping( "/users/{id}/enable" )
   void enableUser(
         @PathVariable( name = "id" ) long id
   );

   @PostMapping( "/users/{id}/roles" )
   void addRole(
         @PathVariable( name = "id" ) long id,
         @RequestBody UserRoleAssignmentDetails userRoleAssignmentDetails
   );

   @GetMapping( "/users/{id}/roles" )
   List< UserRolesDetails > getUserRoles(
         @PathVariable( name = "id" ) long id
   );

}
