package com.kry.Insight;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kry.Insight.persistence.ServiceRepository;
import com.kry.Insight.persistence.ServiceStatusRepository;
import com.kry.Insight.persistence.StructureInitializer;
import com.kry.Insight.persistence.UserRepository;
import com.kry.Insight.service.ServiceStatusScheduler;
import com.kry.Insight.service.ServicesHandler;
import com.kry.Insight.service.UserHandler;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.ext.web.validation.BodyProcessorException;
import io.vertx.json.schema.SchemaRouterOptions;
import io.vertx.json.schema.common.dsl.ObjectSchemaBuilder;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.rxjava3.WriteStreamSubscriber;
import io.vertx.rxjava3.core.AbstractVerticle;
import io.vertx.rxjava3.core.streams.Pump;
import io.vertx.rxjava3.ext.web.Router;
import io.vertx.rxjava3.ext.web.RoutingContext;
import io.vertx.rxjava3.ext.web.client.WebClient;
import io.vertx.rxjava3.ext.web.handler.BodyHandler;
import io.vertx.rxjava3.ext.web.validation.ValidationHandler;
import io.vertx.rxjava3.ext.web.validation.builder.Bodies;
import io.vertx.rxjava3.json.schema.SchemaParser;
import io.vertx.rxjava3.json.schema.SchemaRouter;
import io.vertx.sqlclient.PoolOptions;
import lombok.extern.slf4j.Slf4j;
import io.vertx.rxjava3.ext.web.validation.RequestPredicate;
import io.vertx.rxjava3.mysqlclient.*;

import java.util.concurrent.TimeUnit;

import static io.vertx.json.schema.common.dsl.Keywords.maxLength;
import static io.vertx.json.schema.common.dsl.Keywords.minLength;
import static io.vertx.json.schema.common.dsl.Schemas.objectSchema;
import static io.vertx.json.schema.common.dsl.Schemas.stringSchema;

@Slf4j
public class MainVerticle extends AbstractVerticle {

  static {
    log.info("Customizing the built-in jackson ObjectMapper...");
    var objectMapper = DatabindCodec.mapper();
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    objectMapper.disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS);
    objectMapper.disable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS);

    JavaTimeModule module = new JavaTimeModule();
    objectMapper.registerModule(module);
  }

  @Override
  public Completable rxStart() {
    log.info("Starting HTTP server...");
    InternalLoggerFactory.setDefaultFactory(Slf4JLoggerFactory.INSTANCE);
    Scheduler scheduler = io.vertx.rxjava3.core.RxHelper.scheduler(vertx);
    Flowable<Long> flowable = Flowable.interval(20, TimeUnit.SECONDS, scheduler);

    //Create a PgPool instance
    var mysqlPool = mySqlPool();
    // Initializing the table if not exist
    var initializer = StructureInitializer.create(mysqlPool);
    initializer.run();
    //Creating ServiceRepository
    var serviceRepository = ServiceRepository.create(mysqlPool);
    var statusRepository = ServiceStatusRepository.create(mysqlPool);

    var userRepository = UserRepository.create(mysqlPool);
    var statusScheduler = ServiceStatusScheduler.create(serviceRepository,statusRepository,vertx);

    statusScheduler.statusWriter(flowable);
    //Creating ServicesHandler
    var serviceHandlers = ServicesHandler.create(serviceRepository,statusRepository,vertx);
    var userHandlers = UserHandler.create(userRepository);

    // Configure routes
    var serviceRoutes = serviceRoutes(serviceHandlers,userHandlers);


    // Create the HTTP server
    return vertx.createHttpServer()
      // Handle every request using the router
      .requestHandler(serviceRoutes)
      // Start listening
      .rxListen(8888)
      // convert to Completable.
      .ignoreElement();
  }


  //create routes
  private Router serviceRoutes(ServicesHandler handlers,UserHandler userHandler) {

    // Create a Router
    Router router = Router.router(vertx);
    router.route().handler(io.vertx.rxjava3.ext.web.handler.CorsHandler.create("*")
      .allowedMethod(io.vertx.core.http.HttpMethod.GET)
      .allowedMethod(io.vertx.core.http.HttpMethod.POST)
      .allowedMethod(io.vertx.core.http.HttpMethod.OPTIONS)
      .allowedMethod(io.vertx.core.http.HttpMethod.DELETE)
      .allowedMethod(io.vertx.core.http.HttpMethod.PUT)
      .allowedHeader("Access-Control-Request-Method")
      .allowedHeader("Access-Control-Allow-Credentials")
      .allowedHeader("Access-Control-Allow-Origin")
      .allowedHeader("Access-Control-Allow-Headers")
      .allowedHeader("Content-Type"));

    router.get("/services").produces("application/json").handler(handlers::all);

    router.post("/services").consumes("application/json")
      .handler(BodyHandler.create())
      .handler(handlers::save)
    .produces("application/json");

    router.get("/services-user/:username")
      .handler(handlers::getServiceByUserId);
    router.get("/services/:id").produces("application/json")
      .handler(handlers::get)
      .produces("application/json");

    router.put("/services/:id")
      .consumes("application/json")
      .handler(BodyHandler.create())
      .handler(handlers::update)
      .produces("application/json");;

    router.delete("/services/:id")
      .handler(handlers::delete);

    router.post("/users/signin")
      .consumes("application/json")
      .handler(BodyHandler.create())
      .handler(userHandler::signIn)
      .produces("application/json");;
    return router;
  }
    private MySQLPool mySqlPool() {
    MySQLConnectOptions connectOptions = new MySQLConnectOptions()
      .setPort(3306)
      .setHost("localhost")
      .setDatabase("demo")

      .setUser("user")
      .setPassword("pass");


    // Pool Options
    PoolOptions poolOptions = new PoolOptions().setMaxSize(5);

    // Create the pool from the data object
    MySQLPool pool = MySQLPool.pool( vertx, connectOptions, poolOptions);

    return pool;
  }


}
