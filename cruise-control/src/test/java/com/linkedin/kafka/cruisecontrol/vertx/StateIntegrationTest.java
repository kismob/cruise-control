package com.linkedin.kafka.cruisecontrol.vertx;

import com.linkedin.kafka.cruisecontrol.CruiseControlVertxIntegrationTestHarness;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class StateIntegrationTest extends CruiseControlVertxIntegrationTestHarness {
    @Before
    public void start() throws Exception {
        super.start();
        long startTime = System.currentTimeMillis();
        while((getServletResult("state", _servletPort).split("state: ")[1].split("\\(")[0].equals(getVertxResult("state", _vertxPort).split("state: ")[1].split("\\(")[0])) && (System.currentTimeMillis()-startTime)<60000){
            Thread.sleep(10000);
        }
    }

    @After
    public void teardown() {
        super.stop();
    }

    public void stateResponseNoParams() throws IOException, InterruptedException {
        assertEquals(getServletResult("state", _servletPort), getVertxResult("state", _vertxPort));
    }

}
