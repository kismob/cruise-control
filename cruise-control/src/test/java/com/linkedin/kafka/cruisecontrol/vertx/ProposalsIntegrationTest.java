package com.linkedin.kafka.cruisecontrol.vertx;

import com.linkedin.kafka.cruisecontrol.CruiseControlVertxIntegrationTestHarness;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class ProposalsIntegrationTest extends CruiseControlVertxIntegrationTestHarness {
    @Before
    public void start() throws Exception {
        super.start();
        long startTime = System.currentTimeMillis();
        while((getServletResult("state", _servletPort).split("NumValidWindows: \\(")[1].split("/")[0].equals("0")
                || getVertxResult("state", _vertxPort).split("NumValidWindows: \\(")[1].split("/")[0].equals("0"))
                && (System.currentTimeMillis()-startTime)<60000 * 60){
            Thread.sleep(10000);
        }
    }

    @After
    public void teardown() {
        super.stop();
    }

    public void loadResponse() throws IOException {
        assertEquals(getServletResult("proposals", _servletPort), getVertxResult("proposals", _vertxPort));
        assertEquals(getServletResult("proposals?json=true", _servletPort), getVertxResult("proposals?json=true", _vertxPort));
    }
}
