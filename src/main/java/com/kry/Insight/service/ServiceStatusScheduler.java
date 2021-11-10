package com.kry.Insight.service;

import com.kry.Insight.model.ServiceStatus;
import com.kry.Insight.persistence.ServiceRepository;
import com.kry.Insight.persistence.ServiceStatusRepository;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.client.WebClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServiceStatusScheduler {
  ServiceStatusRepository status;
  ServiceRepository services;
  Vertx vertx;

  public ServiceStatusScheduler(ServiceRepository _services, ServiceStatusRepository _status, Vertx _vertx) {
    this.services = _services;
    this.status = _status;
    this.vertx = _vertx;
  }

  public static ServiceStatusScheduler create(ServiceRepository services, ServiceStatusRepository status, Vertx vertx) {
    return new ServiceStatusScheduler(services, status, vertx);
  }

  public void statusWriter(Flowable<Long> flowable) {
    flowable.subscribe(item -> {
      services.findAll().takeLast(10)
        .subscribe(service -> {
          checkUrl(service.getUrl())
            .subscribe(result -> {
              status.save(ServiceStatus.builder()
                .url(service.getUrl())
                .serviceId(service.getId())
                .name(service.getName())
                .user(service.getUser())
                .status(result)
                .build()).subscribe();
            });
        });
    });
  }

  private @NonNull Single<String> checkUrl(String url) {
    WebClient client = WebClient.create(vertx);
    if (url.contains("https")) {
      return client
        .getAbs(url)
        .ssl(true)
        .rxSend()
        .map(response -> response.statusCode() == 200 ? "OK" : "FAILED")
        .onErrorReturn(err -> {
          log.error(err.getMessage());
          return "ERROR";
        });
    } else {
      return client
        .getAbs(url)
        .rxSend()
        .map(response -> response.statusCode() == 200 ? "OK" : "FAILED")
        .onErrorReturn(err -> {
          log.error(err.getMessage());
          return "ERROR";
        });
    }
  }

}
