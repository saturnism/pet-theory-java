package com.example.pettheory;

import lombok.Data;

@Data
class PubSubMessagePushRequest {
  private PubSubMessage message;
  private String subscription;
}
