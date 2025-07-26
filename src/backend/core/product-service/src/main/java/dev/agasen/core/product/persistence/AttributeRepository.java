package dev.agasen.core.product.persistence;

import dev.agasen.core.product.persistence.entity.Attribute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AttributeRepository extends JpaRepository< Attribute, UUID > {
}
