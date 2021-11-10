package com.kry.Insight.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@Builder
public class ServiceStatus {
  Long id;
  String url;
  Long  serviceId;
  String name;
  String user;
  String status;
  LocalDateTime createdAt;
}
