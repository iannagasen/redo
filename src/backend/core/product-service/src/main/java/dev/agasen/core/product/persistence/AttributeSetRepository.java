package dev.agasen.core.product.persistence;

import dev.agasen.core.product.persistence.entity.AttributeSet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AttributeSetRepository extends JpaRepository< AttributeSet, UUID > {
}
