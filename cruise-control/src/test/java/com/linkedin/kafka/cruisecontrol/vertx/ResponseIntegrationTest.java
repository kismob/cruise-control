package com.linkedin.kafka.cruisecontrol.vertx;

import com.linkedin.kafka.cruisecontrol.CruiseControllVertxIntegrationTestHamess;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;


public class ResponseIntegrationTest extends CruiseControllVertxIntegrationTestHamess {

    private String getServletResult(String endpoint, int port) throws IOException {
        URL servletUrl = new URL("http://localhost:" + port +"/kafkacruisecontrol/" + endpoint);
        HttpURLConnection servletCon = (HttpURLConnection) servletUrl.openConnection();
        servletCon.setRequestMethod("GET");

        BufferedReader servletIn = new BufferedReader(
                new InputStreamReader(servletCon.getInputStream()));
        String servletInputLine;
        StringBuffer servletContent = new StringBuffer();
        while ((servletInputLine = servletIn.readLine()) != null) {
            servletContent.append(servletInputLine);
        }
        servletIn.close();
        return servletContent.toString();
    }

    public String getVertxResult(String endpoint, Integer port) throws IOException {
        URL vertxUrl = new URL("http://localhost:" + port + "/" + endpoint);
        HttpURLConnection vertxCon = (HttpURLConnection) vertxUrl.openConnection();
        vertxCon.setRequestMethod("GET");

        BufferedReader vertxIn = new BufferedReader(
                new InputStreamReader(vertxCon.getInputStream()));
        String vertxInputLine;
        StringBuffer vertxContent = new StringBuffer();
        while ((vertxInputLine = vertxIn.readLine()) != null) {
            vertxContent.append(vertxInputLine);
        }
        vertxIn.close();
        return vertxContent.toString();
    }


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
        Integer servletPort = _servletApp.get_port();
        Integer vertxPort = _vertxApp.get_port();
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
        Integer servletPort = _servletApp.get_port();
        Integer vertxPort = _vertxApp.get_port();
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
        Integer servletPort = _servletApp.get_port();
        Integer vertxPort = _vertxApp.get_port();
        assertEquals(getServletResult("load", servletPort), getVertxResult("load", vertxPort));
        assertEquals(getServletResult("load?json=true", servletPort), getVertxResult("load?json=true", vertxPort));

    }*/
}
