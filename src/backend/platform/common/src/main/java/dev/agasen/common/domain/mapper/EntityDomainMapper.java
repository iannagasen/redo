package dev.agasen.common.domain.mapper;

import java.util.List;

public interface EntityDomainMapper< DOMAIN, ENTITY > {

   ENTITY toEntity( DOMAIN domain );

   default List< ENTITY > toEntity( List< DOMAIN > domain ) {
      return domain.stream().map( this::toEntity ).toList();
   }

}
