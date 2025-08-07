package dev.agasen.core.user.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
@Table( name = "users" )
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
   private boolean deleted;

   @OneToMany( mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true )
   private Set< UserRole > userRoles = new HashSet<>();

   public void addRole( Role role ) {
      if ( hasRole( role ) ) {
         log.warn( "User {} role already exists: {}", this, role );
         return;
      }
      var userRole = new UserRole( null, this, role );
      userRoles.add( userRole );
      log.debug( "User {} role added: {}", this, userRole );
   }

   public boolean hasRole( Role role ) {
      return role.getUserRoles().stream().anyMatch( ur -> ur.getRole().equals( role ) );
   }

   public void addRoles( Collection< Role > roles ) {
      roles.forEach( this::addRole );
   }

}
