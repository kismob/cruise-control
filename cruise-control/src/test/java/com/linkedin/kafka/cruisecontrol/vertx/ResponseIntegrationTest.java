package com.linkedin.kafka.cruisecontrol.vertx;

import com.linkedin.kafka.cruisecontrol.CruiseControlVertxIntegrationTestHarness;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;


public class ResponseIntegrationTest extends CruiseControlVertxIntegrationTestHarness {




    @Before
    public void start() throws Exception {
        super.start();
    }

    @After
    public void teardown() {
        super.stop();
    }

    @Test
    public void testKafkaClusterStateResponses() throws IOException {
        Integer servletPort = _servletApp.getPort();
        Integer vertxPort = _vertxApp.getPort();
        assertEquals(getServletResult("kafka_cluster_state", servletPort), getVertxResult("kafka_cluster_state", vertxPort));
        assertEquals(getServletResult("kafka_cluster_state?json=true", servletPort), getVertxResult("kafka_cluster_state?json=true", vertxPort));
        assertEquals(getServletResult("kafka_cluster_state?verbose=true", servletPort), getVertxResult("kafka_cluster_state?verbose=true", vertxPort));
        assertEquals(getServletResult("kafka_cluster_state?verbose=true&json=true", servletPort), getVertxResult("kafka_cluster_state?verbose=true&json=true", vertxPort));
        //_adminClient.createTopics(Collections.singleton(new NewTopic("topic1", 1, (short) 1))).all().get();
        /*System.out.println(getServletResult("kafka_cluster_state?topic=topic*", servletPort));
        System.out.println(getVertxResult("kafka_cluster_state?topic=topic*"));
        assertEquals(getServletResult("kafka_cluster_state?topic=topic*"), getVertxResult("kafka_cluster_state?topic=topic*"));*/
    }

    @Test
    public void testCruiseControlState() throws IOException {
        Integer servletPort = _servletApp.getPort();
        Integer vertxPort = _vertxApp.getPort();
        assertEquals(getServletResult("state", servletPort), getVertxResult("state", vertxPort));
        assertEquals(getServletResult("state?substates=ANALYZER", servletPort), getVertxResult("state?substates=ANALYZER", vertxPort));
        assertEquals(getServletResult("state?substates=MONITOR", servletPort), getVertxResult("state?substates=MONITOR", vertxPort));
        assertEquals(getServletResult("state?substates=ANOMALY_DETECTOR", servletPort), getVertxResult("state?substates=ANOMALY_DETECTOR", vertxPort));
        assertEquals(getServletResult("state?substates=EXECUTOR", servletPort), getVertxResult("state?substates=EXECUTOR", vertxPort));
        //assertEquals(getServletResult("state?json=true", servletPort), getVertxResult("state?json=true", vertxPort));
        //assertEquals(getServletResult("state?verbose=true", servletPort), getVertxResult("state?verbose=true", vertxPort));
        //assertEquals(getServletResult("state?verbose=true&json=true", servletPort), getVertxResult("state?verbose=true&json=true", vertxPort));
    }

    /*@Test
    public void testLoad() throws IOException{
        Integer servletPort = _servletApp.getPort();
        Integer vertxPort = _vertxApp.getPort();
        assertEquals(getServletResult("load", servletPort), getVertxResult("load", vertxPort));
        assertEquals(getServletResult("load?json=true", servletPort), getVertxResult("load?json=true", vertxPort));

    }*/
}
