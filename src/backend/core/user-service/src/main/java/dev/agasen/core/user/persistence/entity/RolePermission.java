package dev.agasen.core.user.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RolePermission {

   @Id
   @GeneratedValue( strategy = GenerationType.AUTO )
   private Long id;

   @ManyToOne
   private Role role;

   @ManyToOne
   private Permission permission;

   public RolePermission( Role role, Permission permission ) {
      this.role = role;
      this.permission = permission;
   }
}
