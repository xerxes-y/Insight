package com.kry.Insight.service;

import com.kry.Insight.model.CreateServiceCommand;
import com.kry.Insight.model.Service;
import com.kry.Insight.model.UpdateServiceCommand;
import com.kry.Insight.persistence.ServiceNotFoundException;
import com.kry.Insight.persistence.ServiceRepository;
import com.kry.Insight.persistence.ServiceStatusRepository;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.Json;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.RoutingContext;
import io.vertx.rxjava3.ext.web.client.WebClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServicesHandler {

  ServiceRepository services;
  Vertx vertx;
  ServiceStatusRepository serviceStatus;

  private ServicesHandler(ServiceRepository _services, ServiceStatusRepository _status, Vertx _vertx) {
    this.services = _services;
    this.vertx = _vertx;
    this.serviceStatus = _status;
  }

  //factory method
  public static ServicesHandler create(ServiceRepository services, ServiceStatusRepository status, Vertx vertx) {
    return new ServicesHandler(services, status, vertx);
  }

  public void all(RoutingContext rc) {
//        var params = rc.queryParams();
//        var q = params.get("q");
//        var limit = params.get("limit") == null ? 10 : Integer.parseInt(params.get("q"));
//        var offset = params.get("offset") == null ? 0 : Integer.parseInt(params.get("offset"));
//        LOGGER.log(Level.INFO, " find by keyword: q={0}, limit={1}, offset={2}", new Object[]{q, limit, offset});
    this.services.findAll().takeLast(10).toList()
      .subscribe(
        data -> rc.response().end(Json.encode(data))
      );
  }

  public void get(RoutingContext rc) throws ServiceNotFoundException {
    var params = rc.pathParams();
    var id = params.get("id");
    var uuid = Long.parseLong(id);
    this.services.findById(uuid)
      .subscribe(
        post -> rc.response().end(Json.encode(post)),
        throwable -> rc.response().setStatusCode(404).end(Json.encode(throwable.getMessage()))
      );

  }

  public void getServiceByUserId(RoutingContext rc) throws ServiceNotFoundException {
    var params = rc.pathParams();
    var username = params.get("username");
    this.serviceStatus.findServicesByUserId(username).takeLast(10).toList()
      .subscribe(
        data -> rc.response().setChunked(true).setStatusCode(200).end(Json.encode(data)),
        throwable -> rc.response().setStatusCode(403).end(throwable.getMessage())
      );
  }


  public void save(RoutingContext rc) {
    var body = rc.getBodyAsJson();
    log.info("request body: {0}", body);
    var form = body.mapTo(CreateServiceCommand.class);
    checkUrl(form.getService().getUrl())
      .doOnError(throwable -> rc.response().setStatusCode(404).end(Json.encode(throwable.getMessage())))
      .subscribe(status ->
        this.services.save(Service.builder()
          .name(form.getService().getName())
          .url(form.getService().getUrl())
          .user(form.getService().getUser())
          .status(status)
          .build())
          .subscribe(
            savedId -> rc.response()
              .setStatusCode(200)
              .end(),
            throwable -> rc.response().setStatusCode(403).end(Json.encode(throwable.getMessage()))));
  }

  ;


  public void update(RoutingContext rc) {
    var params = rc.pathParams();
    var id = params.get("id");
    var body = rc.getBodyAsJson();
    var form = body.mapTo(UpdateServiceCommand.class);
       checkUrl(form.getService().getUrl()).subscribe(urlStatus-> {
      log.info("\npath param id: {}\nrequest body: {}", id, body);
      this.services.findById(Long.parseLong(id))
        .map(service -> {
            service.setName(form.getService().getName());
            service.setUrl(form.getService().getUrl());
            service.setUser(form.getService().getUser());
            service.setStatus(urlStatus);
            return this.services.update(service).subscribe(serviceId ->{
              serviceStatus.deleteByServiceId(Long.valueOf(id)).subscribe();
            } );
          }
        )
        .subscribe(
          data -> rc.response().setStatusCode(200).end(),
          throwable1 -> rc.response().setStatusCode(403).end(Json.encode(throwable1.getMessage()))
        );
    });


  }

  public void delete(RoutingContext rc) {
    var params = rc.pathParams();
    var id = params.get("id");
    var uuid = Long.parseLong(id);
    this.services.findById(uuid)
      .flatMap(
        service -> {
          return this.serviceStatus.deleteByServiceId(uuid).map(integer -> {
            return this.services.deleteById(Long.parseLong(id)).subscribe();
          });
        }
      )
      .subscribe(
        data -> rc.response().setStatusCode(200).end(),
        throwable -> rc.response().setStatusCode(404).end(Json.encode(throwable.getMessage()))
      );

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
