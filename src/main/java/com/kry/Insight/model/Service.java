package com.kry.Insight.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@Builder
public class Service {
    Long id;
    String name;
    String url;
    String user;
    String status;
    LocalDateTime createdAt;
}
