package com.example.pettheory;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Base64;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class PdfConverterController {

  private static Logger logger = LoggerFactory.getLogger(PdfConverterController.class);

  private final ObjectMapper mapper;

  private final ApplicationContext ctx;

  PdfConverterController(ObjectMapper mapper, ApplicationContext ctx) {
    this.mapper = mapper;
    this.ctx = ctx;
  }

  @PostMapping(
      value = "/",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  String process(@RequestBody Optional<PubSubMessagePushRequest> request) {
    request.ifPresent(
        r -> {
          var bytes = Base64.getDecoder().decode(r.getMessage().getData());
          try {
            var event = mapper.readValue(bytes, GcsEvent.class);
            logger.info("file: " + event);
            logger.info("OK");
          } catch (IOException e) {
            logger.error("error when reading request", e);
          }
        });

    return "OK";
  }
}
