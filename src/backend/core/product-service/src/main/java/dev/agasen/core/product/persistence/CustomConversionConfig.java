package dev.agasen.core.product.persistence;

import dev.agasen.common.persistence.converter.JsonbToMapConverter;
import dev.agasen.common.persistence.converter.MapToJsonbConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;

import java.util.Arrays;

@Configuration
public class CustomConversionConfig extends AbstractJdbcConfiguration {

    @Override
    public JdbcCustomConversions jdbcCustomConversions() {
        return new JdbcCustomConversions(Arrays.asList(
                new MapToJsonbConverter(),
                new JsonbToMapConverter()
        ));
    }
}
