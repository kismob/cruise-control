package com.linkedin.kafka.cruisecontrol.vertx;


import com.linkedin.kafka.cruisecontrol.KafkaCruiseControlApp;
import com.linkedin.kafka.cruisecontrol.servlet.response.CruiseControlState;
import com.linkedin.kafka.cruisecontrol.servlet.response.KafkaClusterState;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.vertx.core.MultiMap;
import io.vertx.ext.web.RoutingContext;

import java.util.*;
import java.util.regex.Pattern;

import static com.linkedin.kafka.cruisecontrol.servlet.response.CruiseControlState.SubState.*;
import static com.linkedin.kafka.cruisecontrol.servlet.response.CruiseControlState.SubState.ANOMALY_DETECTOR;

public class EndPoints {

    @Operation(summary = "Returns Kafka Cluster State", method = "GET", operationId = "kafka_cluster_state",
            tags = {
                    "KafkaClusterState"
            },
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "topic",
                            description = "Regular expression to filter partition state to report based on partition's topic.", schema = @Schema(type = "regex")),
                    @Parameter(in = ParameterIn.QUERY, name = "json",
                            description = "Return in JSON format or not.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "verbose",
                            description = "Return detailed state information.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "doAs",
                            description = "Propagated user by the trusted proxy service.", schema = @Schema(type = "string"))

            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(
                                    mediaType = "application/json",
                                    encoding = @Encoding(contentType = "application/json"),
                                    schema = @Schema(name = "product", example =
                                            "{\"KafkaPartitionState\":{\"offline\":[],\"urp\":[],\"with-offline-replicas\":[],\"under-min-isr\":[]}," +
                                            "\"KafkaBrokerState\":{\"IsController\":{\"0\":true,\"1\":false,\"2\":false},\"OfflineLogDirsByBrokerId\":{\"0\":[],\"1\":[],\"2\":[]},\"ReplicaCountByBrokerId\":{\"0\":44,\"1\":43,\"2\":42},\"OutOfSyncCountByBrokerId\":{},\"Summary\":{\"StdLeadersPerBroker\":0.4714045207910317,\"Leaders\":65,\"MaxLeadersPerBroker\":22,\"Topics\":3,\"MaxReplicasPerBroker\":44,\"StdReplicasPerBroker\":0.816496580927726,\"Brokers\":3,\"AvgReplicationFactor\":1.9846153846153847,\"AvgLeadersPerBroker\":21.666666666666668,\"Replicas\":129,\"AvgReplicasPerBroker\":43.0},\"OnlineLogDirsByBrokerId\":{\"0\":[\"/tmp/kafka-logs-0\"],\"1\":[\"/tmp/kafka-logs-1\"],\"2\":[\"/tmp/kafka-logs-2\"]},\"LeaderCountByBrokerId\":{\"0\":22,\"1\":22,\"2\":21},\"OfflineReplicaCountByBrokerId\":{}},\"version\":1}",
                                            implementation = KafkaClusterState.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Not found."),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error.")
            }
    )
    public void KafkaClusterState(RoutingContext context){
      MultiMap params = context.queryParams();
      KafkaClusterState kafkaClusterState = new KafkaClusterState(KafkaCruiseControlApp.kafkaCruiseControl.kafkaCluster(),
              KafkaCruiseControlApp.kafkaCruiseControl.topicConfigProvider(),
              KafkaCruiseControlApp.kafkaCruiseControl.adminClient(), KafkaCruiseControlApp.kafkaCruiseControl.config());
      Pattern topic = params.get("topic") == null ? null : Pattern.compile(params.get("topic"));
      boolean verbose = Boolean.parseBoolean(params.get("verbose"));
        String outputString = Boolean.parseBoolean(params.get("json")) ?
            kafkaClusterState.getJSONString(verbose, topic) : kafkaClusterState.getPlaintext(verbose, topic);
        context.response()
                .setStatusCode(200)
                .end(outputString);
    }

    @Operation(summary = "Returns Cruise Control State", method = "GET", operationId = "state",
            tags = {
                    "CruiseControlState"
            },
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "substates",
                            description = "Substates for which to retrieve state from cruise-control, available substates are analyzer, monitor, executor and anomaly_detector.", schema = @Schema(type = "list")),
                    @Parameter(in = ParameterIn.QUERY, name = "json",
                            description = "Return in JSON format or not.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "verbose",
                            description = "Return detailed state information.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "super_verbose",
                            description = "Return more detailed state information.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "doAs",
                            description = "Propagated user by the trusted proxy service.", schema = @Schema(type = "string"))

            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(
                                    mediaType = "application/json",
                                    encoding = @Encoding(contentType = "application/json"),
                                    schema = @Schema(name = "product", example =
                                            "{\"KafkaPartitionState\":{\"offline\":[],\"urp\":[],\"with-offline-replicas\":[],\"under-min-isr\":[]}," +
                                                    "\"KafkaBrokerState\":{\"IsController\":{\"0\":true,\"1\":false,\"2\":false},\"OfflineLogDirsByBrokerId\":{\"0\":[],\"1\":[],\"2\":[]},\"ReplicaCountByBrokerId\":{\"0\":44,\"1\":43,\"2\":42},\"OutOfSyncCountByBrokerId\":{},\"Summary\":{\"StdLeadersPerBroker\":0.4714045207910317,\"Leaders\":65,\"MaxLeadersPerBroker\":22,\"Topics\":3,\"MaxReplicasPerBroker\":44,\"StdReplicasPerBroker\":0.816496580927726,\"Brokers\":3,\"AvgReplicationFactor\":1.9846153846153847,\"AvgLeadersPerBroker\":21.666666666666668,\"Replicas\":129,\"AvgReplicasPerBroker\":43.0},\"OnlineLogDirsByBrokerId\":{\"0\":[\"/tmp/kafka-logs-0\"],\"1\":[\"/tmp/kafka-logs-1\"],\"2\":[\"/tmp/kafka-logs-2\"]},\"LeaderCountByBrokerId\":{\"0\":22,\"1\":22,\"2\":21},\"OfflineReplicaCountByBrokerId\":{}},\"version\":1}",
                                            implementation = KafkaClusterState.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Not found."),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error.")
            }
    )
    public void CruiseControlState(RoutingContext context){
        MultiMap params = context.queryParams();
        boolean verbose = Boolean.parseBoolean(params.get("verbose"));
        boolean superVerbose = Boolean.parseBoolean(params.get("super_verbose"));
        boolean json = Boolean.parseBoolean(params.get("json"));
        ArrayList<String> subtateStrings = params.get("substates")== null ? null : new ArrayList<>(Arrays.asList(params.get("substates").split(",")));
        Set<CruiseControlState.SubState> subStateSet = new HashSet<>();
        if (params.get("substates") != null) {
            for (String state : subtateStrings) {
                subStateSet.add(CruiseControlState.SubState.valueOf(state.toUpperCase(Locale.ROOT)));
            }
        }
        Set<CruiseControlState.SubState> substates = !subStateSet.isEmpty() ? subStateSet
                : new HashSet<>(Arrays.asList(CruiseControlState.SubState.values()));
        CruiseControlState cruiseControlState = new CruiseControlState(substates.contains(EXECUTOR) ?
                    KafkaCruiseControlApp.kafkaCruiseControl.executorState() : null,
                substates.contains(MONITOR) ? KafkaCruiseControlApp.kafkaCruiseControl.monitorState(KafkaCruiseControlApp.kafkaCruiseControl.kafkaCluster()) : null,
                substates.contains(ANALYZER) ? KafkaCruiseControlApp.kafkaCruiseControl.analyzerState(KafkaCruiseControlApp.kafkaCruiseControl.kafkaCluster()) : null,
                substates.contains(ANOMALY_DETECTOR) ? KafkaCruiseControlApp.kafkaCruiseControl.anomalyDetectorState() : null,
                KafkaCruiseControlApp.kafkaCruiseControl.config());
        String outputString = json ? cruiseControlState.getJSONString(verbose) : cruiseControlState.getPlaintext(verbose, superVerbose);
        context.response()
                .setStatusCode(200)
                .end(outputString);
    }


}
