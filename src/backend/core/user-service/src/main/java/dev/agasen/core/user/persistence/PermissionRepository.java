package dev.agasen.core.user.persistence;

import dev.agasen.core.user.persistence.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.stream.Collectors;

public interface PermissionRepository extends JpaRepository< Permission, Long > {

   Collection< Permission > findByNameIn( Collection< String > names );

   default Collection< Permission > findAllExistsByNameIn( Collection< String > names ) {
      var permissions = findByNameIn( names );
      var found = permissions.stream().map( Permission::getName ).collect( Collectors.toSet() );
      var missing = names.stream().filter( name -> !found.contains( name ) ).collect( Collectors.toSet() );

      if ( !missing.isEmpty() ) {
         throw new IllegalArgumentException( "Missing permissions: " + missing );
      }

      return permissions;
   }

}
