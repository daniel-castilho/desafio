package dev.matheuslf.desafio.inscritos.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.matheuslf.desafio.inscritos.controller.exception.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ErrorResponseSerializationTest {

    @Autowired
    ObjectMapper mapper;

    @Test
    void errorResponse_instant_serializes_and_deserializes() throws Exception {
        Instant now = Instant.parse("2025-10-20T20:21:51.871908181Z");
        ErrorResponse err = new ErrorResponse(now, 403, "Access Denied", "msg", "/projects");

        String json = mapper.writeValueAsString(err);
        assertNotNull(json);

        var node = mapper.readTree(json);
        assertTrue(node.has("timestamp"));
        assertTrue(node.get("timestamp").isTextual());

        String ts = node.get("timestamp").asText();
        Instant parsed = Instant.parse(ts);
        assertEquals(now, parsed);

        ErrorResponse back = mapper.readValue(json, ErrorResponse.class);
        assertEquals(err, back);
    }
}

