package com.example.pettheory;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class PdfConverterApplication {

  public static void main(String[] args) {
    SpringApplication.run(PdfConverterApplication.class, args);
  }
}

@Data
class PubSubMessage {
  private Map<String, String> attributes;
  private String data;
}

@Data
class PubSubMessagePushRequest {
  private PubSubMessage message;
  private String subscription;
}

@RestController
class PdfConverterController {

  private static Logger logger = LoggerFactory.getLogger(PdfConverterController.class);

  private final ObjectMapper mapper;

  PdfConverterController(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  @PostMapping(value = "/", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  String process(@RequestBody Optional<PubSubMessagePushRequest> request) {
    request.ifPresent(r -> {
      try {
        var bytes = Base64.getDecoder().decode(r.getMessage().getData());
        var json = mapper.readTree(bytes);
        logger.info("file: " + json.toPrettyString());
        logger.info("OK");
      } catch (IOException e) {
        logger.error("error decoding input", e);
      }
    });

    return "OK";
  }
}
