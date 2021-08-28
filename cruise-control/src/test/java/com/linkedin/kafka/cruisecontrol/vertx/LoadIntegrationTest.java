package com.linkedin.kafka.cruisecontrol.vertx;

import com.linkedin.kafka.cruisecontrol.CruiseControlVertxIntegrationTestHarness;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class LoadIntegrationTest extends CruiseControlVertxIntegrationTestHarness {
    @Before
    public void start() throws Exception {
        super.start();
        long startTime = System.currentTimeMillis();
        while((getServletResult("state", _servletPort).split("NumValidWindows: \\(")[1].split("/")[0].equals("0")
                || getVertxResult("state", _vertxPort).split("NumValidWindows: \\(")[1].split("/")[0].equals("0"))
                && (System.currentTimeMillis()-startTime)<60000 * 10){
            Thread.sleep(10000);
        }
    }

    @After
    public void teardown() {
        super.stop();
    }

    public void loadResponse() throws IOException {
        assertEquals(getServletResult("load", _servletPort), getVertxResult("load", _vertxPort));
        assertEquals(getServletResult("load?json=true", _servletPort), getVertxResult("load?json=true", _vertxPort));
    }
}
