package dev.agasen.api.user.user;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class UserRoleAssignmentDetails {

   private List< String > roles;
}
