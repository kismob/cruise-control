package com.linkedin.kafka.cruisecontrol.vertx;


import com.linkedin.kafka.cruisecontrol.KafkaCruiseControlVertxApp;
import com.linkedin.kafka.cruisecontrol.servlet.handler.async.runnable.LoadRunnable;
import com.linkedin.kafka.cruisecontrol.servlet.handler.async.runnable.OperationFuture;
import com.linkedin.kafka.cruisecontrol.servlet.parameters.CruiseControlStateParameters;
import com.linkedin.kafka.cruisecontrol.servlet.parameters.KafkaClusterStateParameters;
import com.linkedin.kafka.cruisecontrol.servlet.parameters.UserTasksParameters;
import com.linkedin.kafka.cruisecontrol.servlet.response.CruiseControlState;
import com.linkedin.kafka.cruisecontrol.servlet.response.KafkaClusterState;
import com.linkedin.kafka.cruisecontrol.servlet.response.UserTaskState;
import com.linkedin.kafka.cruisecontrol.servlet.response.stats.BrokerStats;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.vertx.core.MultiMap;
import io.vertx.ext.web.RoutingContext;

import java.io.UnsupportedEncodingException;
import java.util.*;

import static com.linkedin.kafka.cruisecontrol.servlet.parameters.ParameterUtils.DEFAULT_START_TIME_FOR_CLUSTER_MODEL;
import static com.linkedin.kafka.cruisecontrol.servlet.response.CruiseControlState.SubState.*;
import static com.linkedin.kafka.cruisecontrol.servlet.response.CruiseControlState.SubState.ANOMALY_DETECTOR;

public class EndPoints {

    protected static final int RESPONSE_OK = 200;
    protected static final String TOPIC = "topic";
    protected static final String VERBOSE = "verbose";
    protected static final String SUPER_VERBOSE = "super_verbose";
    protected static final String JSON = "json";
    protected static final String SUBSTATES = "substates";
    protected static final String ALLOW_CAPACITY_ESTIMATION = "allow_capacity_estimation";
    protected static final String START = "start";
    protected static final String END = "end";
    protected static final String TIME = "time";
    protected static final String CAPACITY_ONLY = "capacity_only";
    protected static final String POPULATE_DISC_INFO = "populate_disk_info";
    protected static final String FETCH_COMPLETED_TASK = "fetch_completed_task";
    protected static final String USER_TASK_IDS = "user_task_ids";
    protected static final String CLIENT_IDS = "client_ids";
    protected static final String ENTRIES = "entries";
    protected static final String ENDPOINTS = "endpoints";
    protected static final String TYPES = "types";
    protected static final String STATE = "types";

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
        String topic = params.get(TOPIC) == null ? ".*" : params.get(TOPIC);
        boolean verbose = Boolean.parseBoolean(params.get(VERBOSE));
        boolean json = Boolean.parseBoolean(params.get(JSON));
        KafkaClusterState kafkaClusterState = new KafkaClusterState(KafkaCruiseControlVertxApp.getVerticle().getKafkaCruiseControl().kafkaCluster(),
            KafkaCruiseControlVertxApp.getVerticle().getKafkaCruiseControl().topicConfigProvider(),
            KafkaCruiseControlVertxApp.getVerticle().getKafkaCruiseControl().adminClient(), KafkaCruiseControlVertxApp.getVerticle().getKafkaCruiseControl().config());
        KafkaClusterStateParameters kafkaClusterStateParameters = new KafkaClusterStateParameters();
        try{
            kafkaClusterStateParameters.initParameters(verbose, topic, json);
            String outputString = json ? kafkaClusterState.getJSONString(kafkaClusterStateParameters) : kafkaClusterState.getPlaintext(kafkaClusterStateParameters);
            context.response()
                    .setStatusCode(RESPONSE_OK)
                    .end(outputString);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
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
                            content =
                                @Content(
                                    mediaType = "application/json",
                                    encoding = @Encoding(contentType = "application/json"),
                                    schema = @Schema(name = "jsonState", example =
                                            "{\"AnalyzerState\":{\"isProposalReady\":false,\"readyGoals\":[]},\"MonitorState\":{\"trainingPct\":0.0,\"trained\":false,\"numFlawedPartitions\":0,\"state\":\"RUNNING\",\"numTotalPartitions\":0,\"numMonitoredWindows\":0,\"monitoringCoveragePct\":0.0,\"reasonOfLatestPauseOrResume\":\"N/A\",\"numValidPartitions\":0},\"ExecutorState\":{\"state\":\"NO_TASK_IN_PROGRESS\"},\"AnomalyDetectorState\":{\"recentBrokerFailures\":[],\"recentGoalViolations\":[],\"selfHealingDisabled\":[\"DISK_FAILURE\",\"BROKER_FAILURE\",\"GOAL_VIOLATION\",\"METRIC_ANOMALY\",\"TOPIC_ANOMALY\",\"MAINTENANCE_EVENT\"],\"balancednessScore\":100.0,\"selfHealingEnabled\":[],\"recentDiskFailures\":[],\"metrics\":{\"meanTimeToStartFixMs\":0.0,\"numSelfHealingStarted\":0,\"ongoingAnomalyDurationMs\":0,\"numSelfHealingFailedToStart\":0,\"meanTimeBetweenAnomaliesMs\":{\"DISK_FAILURE\":0.0,\"TOPIC_ANOMALY\":0.0,\"METRIC_ANOMALY\":0.0,\"GOAL_VIOLATION\":0.0,\"BROKER_FAILURE\":0.0,\"MAINTENANCE_EVENT\":0.0}},\"recentMetricAnomalies\":[],\"recentTopicAnomalies\":[],\"selfHealingEnabledRatio\":{\"DISK_FAILURE\":0.0,\"BROKER_FAILURE\":0.0,\"METRIC_ANOMALY\":0.0,\"GOAL_VIOLATION\":0.0,\"TOPIC_ANOMALY\":0.0,\"MAINTENANCE_EVENT\":0.0},\"recentMaintenanceEvents\":[]},\"version\":1}",
                                            implementation = KafkaClusterState.class)
                                )
                    ),
                    @ApiResponse(responseCode = "404", description = "Not found."),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error.")
            }
    )
    public void CruiseControlState(RoutingContext context){
        MultiMap params = context.queryParams();
        boolean verbose = Boolean.parseBoolean(params.get(VERBOSE));
        boolean superVerbose = Boolean.parseBoolean(params.get(SUPER_VERBOSE));
        boolean json = Boolean.parseBoolean(params.get(JSON));
        ArrayList<String> subtateStrings = params.get(SUBSTATES) == null ? null : new ArrayList<>(Arrays.asList(params.get(SUBSTATES).split(",")));
        Set<CruiseControlState.SubState> subStateSet = new HashSet<>();
        if (params.get(SUBSTATES) != null) {
            for (String state : subtateStrings) {
                subStateSet.add(CruiseControlState.SubState.valueOf(state.toUpperCase(Locale.ROOT)));
            }
        }
        Set<CruiseControlState.SubState> substates = !subStateSet.isEmpty() ? subStateSet
                : new HashSet<>(Arrays.asList(CruiseControlState.SubState.values()));
        CruiseControlState cruiseControlState = new CruiseControlState(substates.contains(EXECUTOR)
                ? KafkaCruiseControlVertxApp.getVerticle().getKafkaCruiseControl().executorState() : null,
                substates.contains(MONITOR) ? KafkaCruiseControlVertxApp.getVerticle().getKafkaCruiseControl().monitorState(KafkaCruiseControlVertxApp.getVerticle().getKafkaCruiseControl().kafkaCluster()) : null,
                substates.contains(ANALYZER) ? KafkaCruiseControlVertxApp.getVerticle().getKafkaCruiseControl().analyzerState(KafkaCruiseControlVertxApp.getVerticle().getKafkaCruiseControl().kafkaCluster()) : null,
                substates.contains(ANOMALY_DETECTOR) ? KafkaCruiseControlVertxApp.getVerticle().getKafkaCruiseControl().anomalyDetectorState() : null,
                KafkaCruiseControlVertxApp.getVerticle().getKafkaCruiseControl().config());
        CruiseControlStateParameters cruiseControlStateParameters = new CruiseControlStateParameters();
        try{
        cruiseControlStateParameters.initParameters(json, substates, verbose, superVerbose);
        String outputString = json ? cruiseControlState.getJSONString(cruiseControlStateParameters) : cruiseControlState.getPlaintext(cruiseControlStateParameters);
        context.response()
                .setStatusCode(RESPONSE_OK)
                .end(outputString);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }
    @Operation(summary = "Once Cruise Control Load Monitor shows it is in the RUNNING state, Users can use the following HTTP GET to get the cluster load.", method = "GET", operationId = "load",
            tags = {
                    "Load"
            },
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "start",
                            description = "Start time of the cluster load.", schema = @Schema(type = "long")),
                    @Parameter(in = ParameterIn.QUERY, name = "stop",
                            description = "End time of the cluster load.", schema = @Schema(type = "long")),
                    @Parameter(in = ParameterIn.QUERY, name = "time",
                            description = "End time of the cluster load.", schema = @Schema(type = "long")),
                    @Parameter(in = ParameterIn.QUERY, name = "allow_capacity_estimation",
                            description = "Whether to allow broker capacity to be estimated from other broker in the cluster.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "populate_disk_info",
                            description = "Whether show the load of each disk of broker.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "capacity_only",
                            description = "Whether show only the cluster capacity or the utilization, as well.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "json",
                            description = "Return in JSON format or not.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "verbose",
                            description = "Return detailed state information.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "doAs",
                            description = "Propagated user by the trusted proxy service.", schema = @Schema(type = "string"))

            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    encoding = @Encoding(contentType = "application/json"),
                                    schema = @Schema(name = "jsonState", example =
                                            "{\"version\":1,\"hosts\":[{\"FollowerNwInRate\":0.014293974731117487,\"NwOutRate\":0.06564007233828306,\"NumCore\":3,\"Host\":\"192.168.0.11\",\"CpuPct\":0.08263505232753232,\"Replicas\":130,\"NetworkInCapacity\":110000.0,\"Rack\":\"192.168.0.11\",\"Leaders\":66,\"DiskCapacityMB\":1500000.0,\"DiskMB\":1.094893455505371,\"PnwOutRate\":0.07993405126035213,\"NetworkOutCapacity\":110000.0,\"LeaderNwInRate\":0.0835624267347157,\"DiskPct\":7.299289703369141E-5}],\"brokers\":[{\"FollowerNwInRate\":0.005009444896131754,\"BrokerState\":\"ALIVE\",\"Broker\":0,\"NwOutRate\":0.0048812623135745525,\"NumCore\":1,\"Host\":\"192.168.0.11\",\"CpuPct\":2.4598828167654574E-4,\"Replicas\":44,\"NetworkInCapacity\":50000.0,\"Rack\":\"192.168.0.11\",\"Leaders\":22,\"DiskCapacityMB\":500000.0,\"DiskMB\":0.027019500732421875,\"PnwOutRate\":0.009890707209706306,\"NetworkOutCapacity\":50000.0,\"LeaderNwInRate\":0.0048812623135745525,\"DiskPct\":5.403900146484375E-6},{\"FollowerNwInRate\":0.003591161221265793,\"BrokerState\":\"ALIVE\",\"Broker\":1,\"NwOutRate\":0.0066679054871201515,\"NumCore\":1,\"Host\":\"192.168.0.11\",\"CpuPct\":2.4598828167654574E-4,\"Replicas\":43,\"NetworkInCapacity\":50000.0,\"Rack\":\"192.168.0.11\",\"Leaders\":23,\"DiskCapacityMB\":500000.0,\"DiskMB\":0.03095245361328125,\"PnwOutRate\":0.010259066708385944,\"NetworkOutCapacity\":50000.0,\"LeaderNwInRate\":0.0066679054871201515,\"DiskPct\":6.19049072265625E-6},{\"FollowerNwInRate\":0.00569336861371994,\"BrokerState\":\"ALIVE\",\"Broker\":2,\"NwOutRate\":0.05409090453758836,\"NumCore\":1,\"Host\":\"192.168.0.11\",\"CpuPct\":0.08214307576417923,\"Replicas\":43,\"NetworkInCapacity\":10000.0,\"Rack\":\"192.168.0.11\",\"Leaders\":21,\"DiskCapacityMB\":500000.0,\"DiskMB\":1.036921501159668,\"PnwOutRate\":0.059784277342259884,\"NetworkOutCapacity\":10000.0,\"LeaderNwInRate\":0.072013258934021,\"DiskPct\":2.073843002319336E-4}]}",
                                            implementation = KafkaClusterState.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Not found."),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error.")
            }
    )
    public void Load(RoutingContext context){
        MultiMap params = context.queryParams();
        boolean verbose = Boolean.parseBoolean(params.get(VERBOSE));
        boolean json = Boolean.parseBoolean(params.get(JSON));
        if (params.get(END) != null && params.get(START) != null) {
            throw new IllegalArgumentException("Parameter time and parameter end are mutually exclusive and should not be specified in the same request.");
        }
        long end = params.get(END) == null ? (params.get(TIME) == null ? System.currentTimeMillis() : Long.parseLong(params.get("time"))) : Long.parseLong(params.get("end"));
        Long start = params.get(START) == null ? DEFAULT_START_TIME_FOR_CLUSTER_MODEL : Long.parseLong(params.get(START));
        boolean allow_capacity_estimation = params.get(ALLOW_CAPACITY_ESTIMATION) != null && Boolean.parseBoolean(params.get(ALLOW_CAPACITY_ESTIMATION));
        boolean populate_disk_info = params.get(POPULATE_DISC_INFO) != null && Boolean.parseBoolean(params.get(POPULATE_DISC_INFO));
        boolean capacity_only = params.get(CAPACITY_ONLY) != null && Boolean.parseBoolean(params.get(CAPACITY_ONLY));
        LoadRunnable loadRunnable = new LoadRunnable(KafkaCruiseControlVertxApp.getVerticle().getKafkaCruiseControl(), new OperationFuture("Get broker stats"), start, end,
                allow_capacity_estimation, populate_disk_info, capacity_only);
        try {
            BrokerStats brokerStats = loadRunnable.getResult();
            String outputString = json ? brokerStats.getJSONString() : brokerStats.toString();
            context.response()
                    .setStatusCode(RESPONSE_OK)
                    .end(outputString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Operation(summary = "Returns a full list of all the active/completed(and not recycled) tasks inside Cruise Control.", method = "GET", operationId = "user_tasks",
            tags = {
                    "UserTasks"
            },
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "user_task_ids",
                            description = "Comma separated UUIDs to filter the task results Cruise Control report.", schema = @Schema(type = "list")),
                    @Parameter(in = ParameterIn.QUERY, name = "client_ids",
                            description = "Comma separated IP addresses to filter the task results Cruise Control report.", schema = @Schema(type = "list")),
                    @Parameter(in = ParameterIn.QUERY, name = "entries",
                            description = "Number of partition load entries to report in response.", schema = @Schema(type = "integer")),
                    @Parameter(in = ParameterIn.QUERY, name = "endpoints",
                            description = "Comma separated endpoints to filter the task results Cruise Control report.", schema = @Schema(type = "list")),
                    @Parameter(in = ParameterIn.QUERY, name = "types",
                            description = "Comma separated HTTP request types to filter the task results Cruise Control report.", schema = @Schema(type = "types")),
                    @Parameter(in = ParameterIn.QUERY, name = "capacity_only",
                            description = "Whether show only the cluster capacity or the utilization, as well.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "json",
                            description = "Return in JSON format or not.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "fetch_completed_task",
                            description = "Whether return the original request's final response.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "doAs",
                            description = "Propagated user by the trusted proxy service.", schema = @Schema(type = "string"))

            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    encoding = @Encoding(contentType = "application/json"),
                                    schema = @Schema(name = "jsonState", example =
                                            "{\"version\":1,\"hosts\":[{\"FollowerNwInRate\":0.014293974731117487,\"NwOutRate\":0.06564007233828306,\"NumCore\":3,\"Host\":\"192.168.0.11\",\"CpuPct\":0.08263505232753232,\"Replicas\":130,\"NetworkInCapacity\":110000.0,\"Rack\":\"192.168.0.11\",\"Leaders\":66,\"DiskCapacityMB\":1500000.0,\"DiskMB\":1.094893455505371,\"PnwOutRate\":0.07993405126035213,\"NetworkOutCapacity\":110000.0,\"LeaderNwInRate\":0.0835624267347157,\"DiskPct\":7.299289703369141E-5}],\"brokers\":[{\"FollowerNwInRate\":0.005009444896131754,\"BrokerState\":\"ALIVE\",\"Broker\":0,\"NwOutRate\":0.0048812623135745525,\"NumCore\":1,\"Host\":\"192.168.0.11\",\"CpuPct\":2.4598828167654574E-4,\"Replicas\":44,\"NetworkInCapacity\":50000.0,\"Rack\":\"192.168.0.11\",\"Leaders\":22,\"DiskCapacityMB\":500000.0,\"DiskMB\":0.027019500732421875,\"PnwOutRate\":0.009890707209706306,\"NetworkOutCapacity\":50000.0,\"LeaderNwInRate\":0.0048812623135745525,\"DiskPct\":5.403900146484375E-6},{\"FollowerNwInRate\":0.003591161221265793,\"BrokerState\":\"ALIVE\",\"Broker\":1,\"NwOutRate\":0.0066679054871201515,\"NumCore\":1,\"Host\":\"192.168.0.11\",\"CpuPct\":2.4598828167654574E-4,\"Replicas\":43,\"NetworkInCapacity\":50000.0,\"Rack\":\"192.168.0.11\",\"Leaders\":23,\"DiskCapacityMB\":500000.0,\"DiskMB\":0.03095245361328125,\"PnwOutRate\":0.010259066708385944,\"NetworkOutCapacity\":50000.0,\"LeaderNwInRate\":0.0066679054871201515,\"DiskPct\":6.19049072265625E-6},{\"FollowerNwInRate\":0.00569336861371994,\"BrokerState\":\"ALIVE\",\"Broker\":2,\"NwOutRate\":0.05409090453758836,\"NumCore\":1,\"Host\":\"192.168.0.11\",\"CpuPct\":0.08214307576417923,\"Replicas\":43,\"NetworkInCapacity\":10000.0,\"Rack\":\"192.168.0.11\",\"Leaders\":21,\"DiskCapacityMB\":500000.0,\"DiskMB\":1.036921501159668,\"PnwOutRate\":0.059784277342259884,\"NetworkOutCapacity\":10000.0,\"LeaderNwInRate\":0.072013258934021,\"DiskPct\":2.073843002319336E-4}]}",
                                            implementation = KafkaClusterState.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Not found."),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error.")
            }
    )
    public void userTasks(RoutingContext context) {
        MultiMap params = context.queryParams();
        boolean verbose = Boolean.parseBoolean(params.get(VERBOSE));
        boolean json = Boolean.parseBoolean(params.get(JSON));
        boolean fetch_completed_task = Boolean.parseBoolean(params.get(FETCH_COMPLETED_TASK));
        String userTaskIds = params.get(USER_TASK_IDS) == null ? null : params.get(USER_TASK_IDS);
        String clientIds = params.get(CLIENT_IDS) == null ? null : params.get(CLIENT_IDS);
        String entries = params.get(ENTRIES) == null ? null : params.get(ENTRIES);
        String endpoints = params.get(ENDPOINTS) == null ? null : params.get(ENDPOINTS);
        String types = params.get(TYPES) == null ? null : params.get(TYPES);
        UserTaskState userTaskState = new UserTaskState(KafkaCruiseControlVertxApp.getVerticle().getUserTaskManager().getAllUserTasks(),
                KafkaCruiseControlVertxApp.getVerticle().getKafkaCruiseControl().config());
        UserTasksParameters userTasksParameters = new UserTasksParameters();
        try {

            userTasksParameters.initParameters(json, userTaskIds, clientIds, endpoints, types, entries, fetch_completed_task);
            String outputString = json ? userTaskState.getJSONString(userTasksParameters) : userTaskState.getPlaintext(userTasksParameters);
            context.response()
                    .setStatusCode(RESPONSE_OK)
                    .end(outputString);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
