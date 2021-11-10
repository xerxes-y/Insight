package com.kry.Insight.service;

import com.kry.Insight.model.CreateServiceCommand;
import com.kry.Insight.model.Service;
import com.kry.Insight.persistence.ServiceNotFoundException;
import com.kry.Insight.persistence.ServiceRepository;
import com.kry.Insight.persistence.UserRepository;
import io.vertx.core.json.Json;
import io.vertx.rxjava3.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public class UserHandler {

  UserRepository user;

  private UserHandler(UserRepository _user) {
    this.user = _user;
  }

  //factory method
  public static UserHandler create(UserRepository user) {
    return new UserHandler(user);
  }

  public void signIn(RoutingContext rc) {
    var params = rc.getBodyAsJson();
    var email = params.getString("username");
    var password = params.getString("password");
    user.findByEmailAndPassword(email,password).subscribe(
      data -> rc.response().setStatusCode(200).end(Json.encode(data)),
      throwable -> rc.response().setStatusCode(403).end(Json.encode(throwable.getMessage()))
    );
  }
}
