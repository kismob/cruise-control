/*
 * Copyright 2017 LinkedIn Corp. Licensed under the BSD 2-Clause License (the "License"). See License in the project root for license information.
 */
package com.linkedin.kafka.cruisecontrol;

import com.codahale.metrics.MetricRegistry;
import com.linkedin.kafka.cruisecontrol.async.AsyncKafkaCruiseControl;
import com.linkedin.kafka.cruisecontrol.config.KafkaCruiseControlConfig;
import com.linkedin.kafka.cruisecontrol.vertx.MainVerticle;
import io.vertx.core.Vertx;


public class KafkaCruiseControlVertxApp extends KafkaCruiseControlApp {
    protected static MainVerticle verticle;
    KafkaCruiseControlVertxApp(KafkaCruiseControlConfig config, Integer port, String hostname) {
        super(config, port, hostname);
    }

    //only for tests
    KafkaCruiseControlVertxApp(KafkaCruiseControlConfig config, Integer port,
                               String hostname, AsyncKafkaCruiseControl asyncKafkaCruiseControl, MetricRegistry metricRegistry) {
        super(config, port, hostname, asyncKafkaCruiseControl, metricRegistry);
        Vertx vertx = Vertx.vertx();
        verticle = new MainVerticle(_kafkaCruiseControl, _metricRegistry, _port, _hostname);
        vertx.deployVerticle(verticle);
    }

    @Override
    public String serverUrl() {
        return null;
    }

    @Override
    void start() {
        _kafkaCruiseControl.startUp();
        Vertx vertx = Vertx.vertx();
        verticle = new MainVerticle(_kafkaCruiseControl, _metricRegistry, _port, _hostname);
        vertx.deployVerticle(verticle);
    }

    //only for tests
    /**
     *  Used only for tests
     * @return the verticle
     */
    public static MainVerticle getVerticle() throws Exception {
        if (verticle == null) {
            throw new Exception();
        }
        return verticle;
    }

}
