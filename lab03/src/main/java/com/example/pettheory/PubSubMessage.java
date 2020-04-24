package com.example.pettheory;

import java.util.Map;
import lombok.Data;

@Data
class PubSubMessage {
  private Map<String, String> attributes;
  private String data;
}
