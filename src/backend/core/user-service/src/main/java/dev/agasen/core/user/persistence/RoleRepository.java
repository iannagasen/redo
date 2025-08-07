package dev.agasen.core.user.persistence;

import dev.agasen.core.user.persistence.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.stream.Collectors;

public interface RoleRepository extends JpaRepository< Role, Long > {

   Collection< Role > findByNameIn( Collection< String > names );

   default Collection< Role > findAllExistByNameIn( Collection< String > names ) {
      var roles = findByNameIn( names );
      var found = roles.stream().map( Role::getName ).collect( Collectors.toSet() );
      var missing = names.stream().filter( name -> !found.contains( name ) ).collect( Collectors.toSet() );

      if ( !missing.isEmpty() ) {
         throw new IllegalArgumentException( "Missing roles: " + missing );
      }

      return roles;
   }

}
