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
@Table( name = "roles" )
public class Role {

   @Id
   @GeneratedValue( strategy = GenerationType.AUTO )
   private Long id;

   private String name;
   private String description;

   @OneToMany( mappedBy = "role" )
   private Set< UserRole > userRoles = new HashSet<>();

   @OneToMany( mappedBy = "role", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true )
   private Set< RolePermission > rolePermissions = new HashSet<>();

   public void addPermission( Permission permission ) {
      if ( hasPermission( permission ) ) {
         log.warn( "Permission already exists" );
         return;
      }
      var rolePermission = new RolePermission( this, permission );
      rolePermissions.add( rolePermission );
      log.debug( "Permission added: {}", rolePermission );
   }

   public void addPermissions( Collection< Permission > permissions ) {
      permissions.forEach( this::addPermission );
   }

   public boolean hasPermission( Permission permission ) {
      return rolePermissions.stream().map( RolePermission::getPermission ).anyMatch( permission::equals );
   }
}
