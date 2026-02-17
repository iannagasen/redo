package dev.agasen.api.user.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class UserCreationDetails {

   @NotBlank
   private String username;

   @NotBlank
   @Size( min = 6 )
   private String password;

   @NotBlank
   @Email
   private String email;

   @NotBlank
   private String firstName;

   @NotBlank
   private String lastName;

   private Set< String > roles;

}
