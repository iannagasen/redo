package dev.agasen.api.user.user;

import dev.agasen.api.user.role.RoleDetails;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserDetails {
   private String username;
   private String email;
   private String firstName;
   private String lastName;
   private boolean enabled;
   private boolean locked;
   private Set< RoleDetails > roles;
}
