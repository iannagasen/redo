package dev.agasen.core.user.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Role {

   @Id
   @GeneratedValue( strategy = GenerationType.AUTO )
   private Long id;

   private String name;
   private String description;

   @OneToMany( mappedBy = "role" )
   private Set< UserRole > userRoles;

   @OneToMany( mappedBy = "role" )
   private Set< RolePermission > rolePermissions;

}
