package dev.agasen.core.user.persistence;

import dev.agasen.core.user.persistence.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface RoleRepository extends JpaRepository< Role, Long > {

   Collection< Role > findByNameIn( Collection< String > name );
}
