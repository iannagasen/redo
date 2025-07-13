package dev.agasen.core.product.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.util.HashMap;
import java.util.Map;

@ReadingConverter
class JsonbToMapConverter implements Converter<PGobject, Map<String, Object>> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String, Object> convert(PGobject source) {
        try {
            return objectMapper.readValue(source.getValue(), Map.class);
        } catch (JsonProcessingException e) {
            return new HashMap<>();
        }
    }
}