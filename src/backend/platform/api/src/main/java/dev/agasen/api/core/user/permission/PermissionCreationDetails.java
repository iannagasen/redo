package dev.agasen.api.core.user.permission;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PermissionCreationDetails {
   String name;
   String description;
}
