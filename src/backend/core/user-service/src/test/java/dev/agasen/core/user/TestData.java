package dev.agasen.core.user;

import dev.agasen.api.user.permission.PermissionCreationDetails;
import dev.agasen.api.user.role.RoleCreationDetails;
import dev.agasen.api.user.user.UserCreationDetails;

import java.util.List;
import java.util.Set;

public class TestData {

   public static final List< PermissionCreationDetails > permissionCreationDetails = List.of(
         new PermissionCreationDetails( "USER_CREATE", "Allows creating users" ),
         new PermissionCreationDetails( "USER_DELETE", "Allows deleting users" ),
         new PermissionCreationDetails( "USER_UPDATE", "Allows updating users" ),
         new PermissionCreationDetails( "ROLE_MANAGE", "Allows managing roles" ),
         new PermissionCreationDetails( "CONTENT_CREATE", "Allows creating content" ),
         new PermissionCreationDetails( "CONTENT_UPDATE", "Allows updating content" ),
         new PermissionCreationDetails( "CONTENT_DELETE", "Allows deleting content" ),
         new PermissionCreationDetails( "CONTENT_READ", "Allows reading content" ),
         new PermissionCreationDetails( "USER_READ", "Allows reading user details" )
   );

   public static final List< RoleCreationDetails > roleCreationDetails = List.of(
         new RoleCreationDetails( "ADMIN", "Administrator role with full access", List.of( "USER_CREATE", "USER_DELETE", "USER_UPDATE", "ROLE_MANAGE" ) ),
         new RoleCreationDetails( "EDITOR", "Can edit content but cannot manage users or roles", List.of( "CONTENT_CREATE", "CONTENT_UPDATE", "CONTENT_DELETE" ) ),
         new RoleCreationDetails( "VIEWER", "Read-only access to all resources", List.of( "CONTENT_READ", "USER_READ" ) )
   );

   public static final List< UserCreationDetails > userCreationDetails = List.of(
         new UserCreationDetails( "jdoe", "secret123", "jdoe@example.com", "John", "Doe", Set.of( "ADMIN" ) ),
         new UserCreationDetails( "asmith", "password456", "asmith@example.com", "Alice", "Smith", Set.of( "EDITOR", "ADMIN" ) ),
         new UserCreationDetails( "bjones", "qwerty789", "bjones@example.com", "Bob", "Jones", Set.of( "ADMIN" ) ),
         new UserCreationDetails( "cwhite", "letmein321", "cwhite@example.com", "Carol", "White", Set.of( "EDITOR" ) ),
         new UserCreationDetails( "dking", "dragonpass", "dking@example.com", "David", "King", Set.of( "VIEWER", "ADMIN" ) )
   );

   public static final int permissionCount = permissionCreationDetails.size();
   public static final int roleCount = roleCreationDetails.size();
   public static final int userCount = userCreationDetails.size();

}
