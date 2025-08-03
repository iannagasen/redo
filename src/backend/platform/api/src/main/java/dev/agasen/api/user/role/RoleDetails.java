package dev.agasen.api.user.role;

import dev.agasen.api.user.permission.PermissionDetails;
import lombok.Data;

import java.util.Set;

@Data
public class RoleDetails {
   private String name;
   private String description;
   private Set< PermissionDetails > roles;
}
