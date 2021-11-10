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
public class UpdateServiceCommand implements Serializable {
  Service   service;
  @Data
  public class Service {
    Long id;
    Long serviceId;
    String status;
    String createdAt;
    String name;
    String url;
    String user;
  }
}
