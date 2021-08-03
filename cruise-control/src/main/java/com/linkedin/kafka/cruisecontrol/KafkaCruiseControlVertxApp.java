package com.linkedin.kafka.cruisecontrol;

import com.linkedin.kafka.cruisecontrol.config.KafkaCruiseControlConfig;
import com.linkedin.kafka.cruisecontrol.vertx.MainVerticle;
import io.vertx.core.Vertx;
import javax.servlet.ServletException;



public class KafkaCruiseControlVertxApp extends KafkaCruiseControlApp{
    protected MainVerticle verticle;
    KafkaCruiseControlVertxApp(KafkaCruiseControlConfig config, Integer port, String hostname) throws ServletException {
        super(config, port, hostname);
    }

    @Override
    public String serverUrl() {
        return null;
    }
    @Override
    void start() {
        _kafkaCruiseControl.startUp();
        Vertx vertx = Vertx.vertx();
        verticle= new MainVerticle(_port, _hostname);
        vertx.deployVerticle(verticle);
    }

}
