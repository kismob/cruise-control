package com.linkedin.kafka.cruisecontrol;

import com.linkedin.kafka.cruisecontrol.config.KafkaCruiseControlConfig;
import com.linkedin.kafka.cruisecontrol.config.constants.ExecutorConfig;
import com.linkedin.kafka.cruisecontrol.config.constants.MonitorConfig;
import com.linkedin.kafka.cruisecontrol.metricsreporter.utils.CCEmbeddedBroker;
import com.linkedin.kafka.cruisecontrol.metricsreporter.utils.CCKafkaIntegrationTestHarness;
import com.linkedin.kafka.cruisecontrol.monitor.sampling.KafkaSampleStore;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.junit.Before;

import java.net.ServerSocket;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;


public class CruiseControllVertxIntegrationTestHamess extends CCKafkaIntegrationTestHarness{
    protected KafkaCruiseControlConfig _config;
    protected KafkaCruiseControlServletApp _servletApp;
    protected KafkaCruiseControlVertxApp _vertxApp;
    protected AdminClient _adminClient;

    protected static final String LOCALHOST = "localhost";

    protected Map<String, Object> withConfigs() {
        return Collections.emptyMap();
    }

    private void setupConfig() {
        Properties properties = KafkaCruiseControlUnitTestUtils.getKafkaCruiseControlProperties();
        properties.put(MonitorConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers());
        properties.put(ExecutorConfig.ZOOKEEPER_CONNECT_CONFIG, zkConnect());
        properties.put(KafkaSampleStore.PARTITION_METRIC_SAMPLE_STORE_TOPIC_CONFIG, "__partition_samples");
        properties.put(KafkaSampleStore.BROKER_METRIC_SAMPLE_STORE_TOPIC_CONFIG, "__broker_samples");
        properties.putAll(withConfigs());
        _config = new KafkaCruiseControlConfig(properties);
    }

    public void start() throws Exception {
        super.setUp();
        _brokers.values().forEach(CCEmbeddedBroker::startup);
        setupConfig();
        _vertxApp = new KafkaCruiseControlVertxApp(_config, new ServerSocket(0).getLocalPort(), LOCALHOST);
        _vertxApp.start();
        _servletApp = new KafkaCruiseControlServletApp(_config, new ServerSocket(0).getLocalPort(), LOCALHOST);
        System.out.println(_servletApp.serverUrl());
        _servletApp.start();
        Properties properties = new Properties();
        properties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers());
        _adminClient = AdminClient.create(properties);
    }

    public void stop() {
        _adminClient.close();
        if (_vertxApp != null) {
            _vertxApp.stop();
        }
        if (_servletApp != null) {
            _servletApp.stop();
        }
        _brokers.values().forEach(CCEmbeddedBroker::shutdown);
        _brokers.values().forEach(CCEmbeddedBroker::awaitShutdown);
        super.tearDown();
    }
}
