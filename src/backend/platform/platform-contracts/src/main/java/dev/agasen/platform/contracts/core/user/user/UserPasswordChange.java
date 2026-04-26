package dev.agasen.platform.contracts.core.user.user;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserPasswordChange {
   private String currentPassword;
   private String newPassword;
}
