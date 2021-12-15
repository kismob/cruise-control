/*
 * Copyright 2021 LinkedIn Corp. Licensed under the BSD 2-Clause License (the "License"). See License in the project root for license information.
 */
package com.linkedin.kafka.cruisecontrol.vertx;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.linkedin.kafka.cruisecontrol.async.AsyncKafkaCruiseControl;
import com.linkedin.kafka.cruisecontrol.vertx.generator.OpenApiRoutePublisher;
import com.linkedin.kafka.cruisecontrol.vertx.generator.Required;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.ErrorHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import org.apache.commons.lang3.reflect.FieldUtils;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;

public class MainVerticle extends AbstractVerticle {

  public static final String APPLICATION_JSON = "application/json";
  private int _port;
  private String _host;
  private HttpServer _server;
  private SwaggerApi _endPoints;
  private AsyncKafkaCruiseControl _asynckafkaCruiseControl;
  private MetricRegistry _dropwizardMetricRegistry;

  public MainVerticle(AsyncKafkaCruiseControl asynckafkaCruiseControl, MetricRegistry dropwizardMetricRegistry, int port, String host) {
    _port = port;
    _host = host;
    _asynckafkaCruiseControl = asynckafkaCruiseControl;
    _dropwizardMetricRegistry = dropwizardMetricRegistry;

  }

  public HttpServer getServer() {
    return _server;
  }

  @Override
  public void start(Future<Void> startFuture) throws Exception {

    _endPoints = new EndPoints(_asynckafkaCruiseControl, _dropwizardMetricRegistry);

    _server = vertx.createHttpServer(createOptions());
    _server.requestHandler(configurationRouter()::accept);
    _server.listen(result -> {
      if (result.succeeded()) {
        startFuture.complete();
      } else {
        startFuture.fail(result.cause());
      }
    });
  }

  @Override
  public void stop(Future<Void> future) {
    _endPoints.destroy();
    if (_server == null) {
      future.complete();
      return;
    }
    _server.close(future.completer());
  }

  private HttpServerOptions createOptions() {
    HttpServerOptions options = new HttpServerOptions();
    options.setHost(_host);
    options.setPort(_port);
    return options;
  }

  private Router configurationRouter() {
    Router router = Router.router(vertx);
    router.route().consumes(APPLICATION_JSON);
    router.route().produces(APPLICATION_JSON);
    router.route().handler(BodyHandler.create());

    Set<String> allowedHeaders = new HashSet<>();
    allowedHeaders.add("auth");
    allowedHeaders.add("Content-Type");

    Set<HttpMethod> allowedMethods = new HashSet<>();
    allowedMethods.add(HttpMethod.GET);
    allowedMethods.add(HttpMethod.POST);
    allowedMethods.add(HttpMethod.OPTIONS);
    allowedMethods.add(HttpMethod.DELETE);
    allowedMethods.add(HttpMethod.PATCH);
    allowedMethods.add(HttpMethod.PUT);

    router.route().handler(CorsHandler.create("*").allowedHeaders(allowedHeaders).allowedMethods(allowedMethods));
    router.route().handler(CorsHandler.create("*").allowedHeaders(allowedHeaders).allowedMethods(allowedMethods));
    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));

    router.route().handler(context -> {
      context.next();
    });
    router.route().failureHandler(ErrorHandler.create(true));

    // Routing section - this is where we declare which end points we want to use
    router.get("/kafka_cluster_state").handler(_endPoints::kafkaClusterState);
    router.get("/state").handler(_endPoints::cruiseControlState);
    router.get("/load").handler(_endPoints::load);
    router.get("/user_tasks").handler(_endPoints::userTasks);
    router.get("/partition_load").handler(_endPoints::partitionLoad);
    router.get("/proposals").handler(_endPoints::proposals);
    router.post("/rebalance").handler(_endPoints::rebalance);
    router.post("/add_broker").handler(_endPoints::addBroker);
    router.post("/remove_broker").handler(_endPoints::removeBroker);
    router.post("/fix_offline_replicas").handler(_endPoints::fixOfflineReplicas);
    router.post("/demote_broker").handler(_endPoints::demoteBroker);
    router.post("/stop_proposal_execution").handler(_endPoints::stopProposalExecution);
    router.post("/pause_sampling").handler(_endPoints::pauseSampling);
    router.post("/resume_sampling").handler(_endPoints::resumeSampling);
    router.post("/topic_configuration").handler(_endPoints::topicConfiguration);
    router.post("/admin").handler(_endPoints::admin);
    router.post("/rightsize").handler(_endPoints::rightsize);

    OpenAPI openAPIDoc = OpenApiRoutePublisher.publishOpenApiSpec(
      router,
      "spec",
      "Cruise Control Swagger",
      "1.0.0",
      "http://" + _host + ":" + _port + "/"
    );

        // Generate the SCHEMA section of Swagger, using the definitions in the Model folder
        ImmutableSet<ClassPath.ClassInfo> modelClasses = getClassesInPackage("io.vertx.VertxAutoSwagger.Model");

        Map<String, Object> map = new HashMap<String, Object>();

        for (ClassPath.ClassInfo modelClass : modelClasses) {

          Field[] fields = FieldUtils.getFieldsListWithAnnotation(modelClass.load(), Required.class).toArray(new
                Field[0]);
          List<String> requiredParameters = new ArrayList<String>();

          for (Field requiredField : fields) {
              requiredParameters.add(requiredField.getName());
          }

          fields = modelClass.load().getDeclaredFields();

          for (Field field : fields) {
            mapParameters(field, map);
          }

          openAPIDoc.schema(modelClass.getSimpleName(),
            new Schema()
              .title(modelClass.getSimpleName())
              .type("object")
              .required(requiredParameters)
              .properties(map)
          );

          map = new HashMap<String, Object>();
        }
    //

    // Serve the Swagger JSON spec out on /swagger
    router.get("/swagger").handler(res -> {
      res.response()
        .setStatusCode(200)
        .end(Json.pretty(openAPIDoc));
    });

    // Serve the Swagger UI out on /doc/index.html
    router.route("/doc/*").handler(StaticHandler.create().setCachingEnabled(false).setWebRoot("webroot/node_modules/swagger-ui-dist"));

    return router;
  }

  private void mapParameters(Field field, Map<String, Object> map) {
    Class type = field.getType();
    Class componentType = field.getType().getComponentType();

    if (isPrimitiveOrWrapper(type)) {
      Schema primitiveSchema = new Schema();
      primitiveSchema.type(field.getType().getSimpleName());
      map.put(field.getName(), primitiveSchema);
    } else {
      HashMap<String, Object> subMap = new HashMap<String, Object>();

      if (isPrimitiveOrWrapper(componentType)) {
        HashMap<String, Object> arrayMap = new HashMap<String, Object>();
        arrayMap.put("type", componentType.getSimpleName() + "[]");
        subMap.put("type", arrayMap);
      } else {
        subMap.put("$ref", "#/components/schemas/" + componentType.getSimpleName());
      }

      map.put(field.getName(), subMap);
    }
  }

  private Boolean isPrimitiveOrWrapper(Type type) {
    return type.equals(Double.class)
            || type.equals(Float.class)
            || type.equals(Long.class)
            || type.equals(Integer.class)
            || type.equals(Short.class)
            || type.equals(Character.class)
            || type.equals(Byte.class)
            || type.equals(Boolean.class)
            || type.equals(String.class)
            || type.equals(ArrayList.class)
            || type.equals(Map.class);
  }

  public ImmutableSet<ClassPath.ClassInfo> getClassesInPackage(String pckgname) {
    try {
      ClassPath classPath = ClassPath.from(Thread.currentThread().getContextClassLoader());
      ImmutableSet<ClassPath.ClassInfo> classes = classPath.getTopLevelClasses(pckgname);
      return classes;

    } catch (Exception e) {
      return null;
    }
  }

  public SwaggerApi getEndPoints() {
    return _endPoints;
  }

}
