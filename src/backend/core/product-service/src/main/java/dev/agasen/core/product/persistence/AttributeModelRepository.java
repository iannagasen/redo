package dev.agasen.core.product.persistence;

import dev.agasen.core.product.persistence.entity.AttributeModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AttributeModelRepository extends JpaRepository< AttributeModel, UUID > {
}
