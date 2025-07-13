package dev.agasen.core.product.domain;

import java.util.List;

public interface EntityDomainMapper<DOMAIN, ENTITY> {

    ENTITY toEntity(DOMAIN domain);

    DOMAIN toDomain(ENTITY entity);

    default List<ENTITY> toEntity(List<DOMAIN> domain) {
        return domain.stream().map(this::toEntity).toList();
    }

    default List<DOMAIN> toDomain(List<ENTITY> entity) {
        return entity.stream().map(this::toDomain).toList();
    }
}
