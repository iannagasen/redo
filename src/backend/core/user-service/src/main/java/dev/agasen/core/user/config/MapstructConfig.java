package dev.agasen.core.user.config;

import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

@MapperConfig( unmappedTargetPolicy = ReportingPolicy.ERROR )
public class MapstructConfig {
}
