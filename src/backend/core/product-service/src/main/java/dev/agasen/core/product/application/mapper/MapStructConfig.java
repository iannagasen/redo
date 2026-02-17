package dev.agasen.core.product.application.mapper;

import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

@MapperConfig( unmappedTargetPolicy = ReportingPolicy.ERROR )
public class MapStructConfig {
}
