package dev.agasen.core.user.persistence;

import dev.agasen.core.user.persistence.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository< User, Long > {
}
