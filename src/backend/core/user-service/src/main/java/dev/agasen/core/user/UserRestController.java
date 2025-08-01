package dev.agasen.core.user;

import dev.agasen.api.user.UserRestControllerSpec;
import dev.agasen.api.user.user.*;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserRestController implements UserRestControllerSpec {

   @Override
   public Page< UserDetails > getUsers( int page, int size, List< String > roles, List< String > permissions ) {
      return null;
   }

   @Override
   public UserDetails getUser( long id ) {
      return null;
   }

   @Override
   public void createUser( UserCreationDetails userCreationDetails ) {

   }

   @Override
   public void updateUser( UserModificationDetails userModificationDetails ) {

   }

   @Override
   public void deleteUser( long id ) {

   }

   @Override
   public void enableUser( long id ) {

   }

   @Override
   public void addRole( long id, UserRoleAssignmentDetails userRoleAssignmentDetails ) {

   }

   @Override
   public List< UserRolesDetails > getUserRoles( long id ) {
      return List.of();
   }
}
