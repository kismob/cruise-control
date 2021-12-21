/*
 * Copyright 2021 LinkedIn Corp. Licensed under the BSD 2-Clause License (the "License"). See License in the project root for license information.
 */
package com.linkedin.kafka.cruisecontrol.vertx;

import com.codahale.metrics.MetricRegistry;
import com.linkedin.kafka.cruisecontrol.async.AsyncKafkaCruiseControl;
import com.linkedin.kafka.cruisecontrol.config.constants.WebServerConfig;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.ErrorHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.openapi.RouterBuilder;
import io.vertx.ext.web.sstore.LocalSessionStore;
import java.util.HashSet;
import java.util.Set;

public class MainVerticle extends AbstractVerticle {

  public static final String APPLICATION_JSON = "application/json";
  private int _port;
  private String _host;
  private HttpServer _server;
  private VertxHandler _endPoints;
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
  public void start(Promise<Void> startPromise) throws Exception {

    _endPoints = new VertxHandler(_asynckafkaCruiseControl, _dropwizardMetricRegistry);

    RouterBuilder.create(vertx, this.getClass().getClassLoader().getResource("yaml/base.yaml").toString(), asyncResult -> {
      if (!asyncResult.succeeded()) {
        throw new RuntimeException(asyncResult.cause());
      } else {
        _server = vertx.createHttpServer(createOptions());
        _server.requestHandler(buildRouter(asyncResult.result()));
        _server.listen(result -> {
          if (result.succeeded()) {
            startPromise.complete();
          } else {
            startPromise.fail(result.cause());
          }
        });
      }
    });
  }

  @Override
  public void stop(Promise<Void> promise) {
    _endPoints.destroy();
    if (_server == null) {
      return;
    }
    _server.close();
  }

  private Router buildRouter(RouterBuilder builder) {
    builder.operation("state").handler(_endPoints::handle);
    builder.operation("kafkaClusterState").handler(_endPoints::handle);
    builder.operation("load").handler(_endPoints::handle);
    builder.operation("userTasks").handler(_endPoints::handle);
    builder.operation("partitionload").handler(_endPoints::handle);
    builder.operation("proposals").handler(_endPoints::handle);
    builder.operation("rebalance").handler(_endPoints::handle);
    builder.operation("addBroker").handler(_endPoints::handle);
    builder.operation("removeBroker").handler(_endPoints::handle);
    builder.operation("fixOfflineReplicas").handler(_endPoints::handle);
    builder.operation("demoteBroker").handler(_endPoints::handle);
    builder.operation("stopProposalExecution").handler(_endPoints::handle);
    builder.operation("pauseSampling").handler(_endPoints::handle);
    builder.operation("resumeSampling").handler(_endPoints::handle);
    builder.operation("topicConfiguration").handler(_endPoints::handle);
    builder.operation("admin").handler(_endPoints::handle);
    builder.operation("rightsize").handler(_endPoints::handle);
    builder.rootHandler(StaticHandler
            .create()
            .setCachingEnabled(false)
            .setWebRoot("webroot/"));
    Router router = builder.createRouter();

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

    router.route().handler(RoutingContext::next);
    router.route().failureHandler(ErrorHandler.create(vertx, true));

    Router root = Router.router(vertx);
    String rootPath = _asynckafkaCruiseControl
            .config()
            .getString(WebServerConfig.WEBSERVER_API_URLPREFIX_CONFIG)
            .trim()
            .replace("/*", "/");
    root.mountSubRouter(rootPath, router);

    return root;
  }

  private HttpServerOptions createOptions() {
    HttpServerOptions options = new HttpServerOptions();
    options.setHost(_host);
    options.setPort(_port);
    return options;
  }

  public VertxHandler getEndPoints() {
    return _endPoints;
  }

}
