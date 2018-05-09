package pl.speedapp.cargame.db.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.IOException;

@Converter
@Slf4j
public class GameMapRoadJsonConverter implements AttributeConverter<int[][], String> {
    @Override
    public String convertToDatabaseColumn(int[][] map) {
        try {
            ObjectMapper om = new ObjectMapper();
            return om.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            log.error("Error while converting GameMap road array to JSON", e);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int[][] convertToEntityAttribute(String mapJsonString) {
        try {
            ObjectMapper om = new ObjectMapper();
            return om.readValue(mapJsonString, int[][].class);
        } catch (IOException e) {
            log.error("Error while converting GameMap road JSON to array", e);
            e.printStackTrace();
        }
        return null;
    }
}
