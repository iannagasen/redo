package dev.agasen.api.core.user.role;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RoleCreationDetails {

   private String name;
   private String description;
   private List< String > permissions;

}
