package dev.agasen.core.product.config;

import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

@MapperConfig( unmappedTargetPolicy = ReportingPolicy.ERROR )
public class MapStructConfig {
}
