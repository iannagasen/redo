package dev.agasen.platform.contracts.core.user.role;

import dev.agasen.platform.contracts.core.user.permission.PermissionDetails;
import lombok.Data;

import java.util.Set;

@Data
public class RoleDetails {
   private String name;
   private String description;
   private Set< PermissionDetails > permissions;
}
