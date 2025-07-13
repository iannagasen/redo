package dev.agasen.core.product.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import java.sql.SQLException;
import java.util.Map;

@WritingConverter
class MapToJsonbConverter implements Converter<Map<String, Object>, PGobject> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public PGobject convert(Map<String, Object> source) {
        PGobject jsonObject = new PGobject();
        jsonObject.setType("jsonb");
        try {
            jsonObject.setValue(objectMapper.writeValueAsString(source));
        } catch (SQLException | JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return jsonObject;
    }
}