package com.linkedin.kafka.cruisecontrol.vertx;

import com.linkedin.kafka.cruisecontrol.CruiseControlVertxIntegrationTestHarness;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class KafkaClusterStateIntegrationTest extends CruiseControlVertxIntegrationTestHarness {
    @Before
    public void start() throws Exception {
        super.start();
        long startTime = System.currentTimeMillis();
        while(getVertxResult("kafka_cluster_state", _vertxPort).split(" ")[6].equals("0") && (System.currentTimeMillis()-startTime)<60000){
            Thread.sleep(10000);
        }
    }

    @After
    public void teardown() {
        super.stop();
    }

    public void testKafkaClusterStateResponse() throws IOException {
        assertEquals(getServletResult("kafka_cluster_state", _servletPort), getVertxResult("kafka_cluster_state", _vertxPort));
        assertEquals(getServletResult("kafka_cluster_state?json=true", _servletPort), getVertxResult("kafka_cluster_state?json=true", _vertxPort));
        assertEquals(getServletResult("kafka_cluster_state?verbose=true", _servletPort), getVertxResult("kafka_cluster_state?verbose=true", _vertxPort));
        assertEquals(getServletResult("kafka_cluster_state?verbose=true&json=true", _servletPort), getVertxResult("kafka_cluster_state?verbose=true&json=true", _vertxPort));

    }
}
