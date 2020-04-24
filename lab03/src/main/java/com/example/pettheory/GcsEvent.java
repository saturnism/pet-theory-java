package com.example.pettheory;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class GcsEvent {

  private String bucket;
  private String name;
}
