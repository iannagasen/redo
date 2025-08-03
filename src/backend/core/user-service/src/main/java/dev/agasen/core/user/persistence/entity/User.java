package dev.agasen.core.user.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {

   @Id
   @GeneratedValue( strategy = GenerationType.AUTO )
   private Long id;

   private String username;
   private String password;
   private String email;

   private String firstName;
   private String lastName;

   private boolean enabled;
   private boolean locked;

   @OneToMany( mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true )
   private Set< UserRole > userRoles = new HashSet<>();
}
