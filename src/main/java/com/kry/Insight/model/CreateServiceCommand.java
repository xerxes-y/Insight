package com.kry.Insight.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateServiceCommand implements Serializable {
  Service service;
  @Data
  public class Service {
    String name;
    String url;
    String user;
  }
}
