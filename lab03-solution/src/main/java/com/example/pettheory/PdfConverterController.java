package com.example.pettheory;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import java.io.IOException;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gcp.storage.GoogleStorageResource;
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

  private final String outputBucket;

  PdfConverterController(
      ObjectMapper mapper, ApplicationContext ctx, @Value("${PDF_BUCKET}") String outputBucket) {
    this.mapper = mapper;
    this.ctx = ctx;
    this.outputBucket = outputBucket;
  }

  private void convert(GcsEvent event)
      throws IOException, ExecutionException, InterruptedException {
    // Read the file from Cloud Storage
    var input = ctx.getResource(String.format("gs://%s/%s", event.getBucket(), event.getName()));
    if (!input.exists()) {
      logger.info("does not exist: " + event);
      return;
    }

    // Generate the output file name
    String outputFileName = event.getName().replaceAll("\\.docx", ".pdf");

    // Output to Cloud Storage
    var output =
        (GoogleStorageResource)
            ctx.getResource(String.format("gs://%s/%s", outputBucket, outputFileName));

    try (var is = input.getInputStream()) {
      try (var os = output.getOutputStream()) {
        logger.info("converting...");
        var doc = new XWPFDocument(is);
        PdfConverter.getInstance().convert(doc, os, PdfOptions.create());
      }
    }
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

            convert(event);

            logger.info("OK");
          } catch (IOException | ExecutionException | InterruptedException e) {
            logger.error("error when converting file", e);
          }
        });

    return "OK";
  }
}
