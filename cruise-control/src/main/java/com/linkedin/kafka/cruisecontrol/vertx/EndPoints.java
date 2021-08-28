/*
 * Copyright 2018 LinkedIn Corp. Licensed under the BSD 2-Clause License (the "License"). See License in the project root for license information.
 */

package com.linkedin.kafka.cruisecontrol.vertx;


import com.linkedin.cruisecontrol.servlet.parameters.CruiseControlParameters;
import com.linkedin.cruisecontrol.servlet.response.CruiseControlResponse;
import com.linkedin.kafka.cruisecontrol.KafkaCruiseControlVertxApp;
import com.linkedin.kafka.cruisecontrol.commonapi.CommonApi;
import com.linkedin.kafka.cruisecontrol.servlet.handler.async.runnable.GetStateRunnable;
import com.linkedin.kafka.cruisecontrol.servlet.handler.async.runnable.LoadRunnable;
import com.linkedin.kafka.cruisecontrol.servlet.handler.async.runnable.OperationFuture;
import com.linkedin.kafka.cruisecontrol.servlet.handler.async.runnable.PartitionLoadRunnable;
import com.linkedin.kafka.cruisecontrol.servlet.handler.async.runnable.ProposalsRunnable;
import com.linkedin.kafka.cruisecontrol.servlet.parameters.ClusterLoadParameters;
import com.linkedin.kafka.cruisecontrol.servlet.parameters.CruiseControlStateParameters;
import com.linkedin.kafka.cruisecontrol.servlet.parameters.KafkaClusterStateParameters;
import com.linkedin.kafka.cruisecontrol.servlet.parameters.UserTasksParameters;
import com.linkedin.kafka.cruisecontrol.servlet.parameters.PartitionLoadParameters;
import com.linkedin.kafka.cruisecontrol.servlet.parameters.ProposalsParameters;
import com.linkedin.kafka.cruisecontrol.servlet.response.CruiseControlState;
import com.linkedin.kafka.cruisecontrol.servlet.response.KafkaClusterState;
import com.linkedin.kafka.cruisecontrol.servlet.response.PartitionLoadState;
import com.linkedin.kafka.cruisecontrol.servlet.response.UserTaskState;
import com.linkedin.kafka.cruisecontrol.servlet.response.stats.BrokerStats;
import com.linkedin.kafka.cruisecontrol.servlet.response.OptimizationResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.vertx.core.MultiMap;
import io.vertx.ext.web.RoutingContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.HashSet;
import java.util.regex.Pattern;

import static com.linkedin.kafka.cruisecontrol.servlet.parameters.ParameterUtils.*;

public class EndPoints {

    protected static final int RESPONSE_OK = 200;

    private void useUserTaskManager(RoutingContext context, CruiseControlParameters parameters, CruiseControlResponse response) throws Exception {
        CommonApi common = new CommonApi(context);
        int step = 0;
        KafkaCruiseControlVertxApp.getVerticle().getUserTaskManager().getOrCreateUserTask(common,
                uuid -> {
                    OperationFuture future = new OperationFuture(String.format("%s request", parameters.endPoint().toString()));
                    future.complete(response);
                    return future; }, step, false, parameters
        );
    }

    @Operation(summary = "Gives partition healthiness on the cluster.", method = "GET", operationId = "kafka_cluster_state",
            tags = {
                    "kafkaClusterState"
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
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    encoding = @Encoding(contentType = "application/json"),
                                    schema = @Schema(name = "jsonState", example =
                                            "{\\\"KafkaPartitionState\\\":{\\\"offline\\\":[],\\\"urp\\\":[],\\\"with-offline-replicas\\\":[],\\\"under-min-isr\\\":[]},\\\"KafkaBrokerState\\\":{\\\"IsController\\\":{\\\"0\\\":true,\\\"1\\\":false,\\\"2\\\":false},\\\"OfflineLogDirsByBrokerId\\\":{\\\"0\\\":[],\\\"1\\\":[],\\\"2\\\":[]},\\\"ReplicaCountByBrokerId\\\":{\\\"0\\\":44,\\\"1\\\":43,\\\"2\\\":42},\\\"OutOfSyncCountByBrokerId\\\":{},\\\"Summary\\\":{\\\"StdLeadersPerBroker\\\":0.4714045207910317,\\\"Leaders\\\":65,\\\"MaxLeadersPerBroker\\\":22,\\\"Topics\\\":3,\\\"MaxReplicasPerBroker\\\":44,\\\"StdReplicasPerBroker\\\":0.816496580927726,\\\"Brokers\\\":3,\\\"AvgReplicationFactor\\\":1.9846153846153847,\\\"AvgLeadersPerBroker\\\":21.666666666666668,\\\"Replicas\\\":129,\\\"AvgReplicasPerBroker\\\":43.0},\\\"OnlineLogDirsByBrokerId\\\":{\\\"0\\\":[\\\"/tmp/kafka-logs-0\\\"],\\\"1\\\":[\\\"/tmp/kafka-logs-1\\\"],\\\"2\\\":[\\\"/tmp/kafka-logs-2\\\"]},\\\"LeaderCountByBrokerId\\\":{\\\"0\\\":22,\\\"1\\\":22,\\\"2\\\":21},\\\"OfflineReplicaCountByBrokerId\\\":{}},\\\"version\\\":1}",
                                            implementation = KafkaClusterState.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Not found."),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error.")
            }
    )
    public void kafkaClusterState(RoutingContext context) {
        MultiMap params = context.queryParams();
        String topic = params.get(TOPIC_PARAM) == null ? ".*" : params.get(TOPIC_PARAM);
        boolean verbose = Boolean.parseBoolean(params.get(VERBOSE_PARAM));
        boolean json = Boolean.parseBoolean(params.get(JSON_PARAM));
        KafkaClusterState kafkaClusterState = new KafkaClusterState(KafkaCruiseControlVertxApp.getVerticle().getKafkaCruiseControl().kafkaCluster(),
            KafkaCruiseControlVertxApp.getVerticle().getKafkaCruiseControl().topicConfigProvider(),
            KafkaCruiseControlVertxApp.getVerticle().getKafkaCruiseControl().adminClient(), KafkaCruiseControlVertxApp.getVerticle().getKafkaCruiseControl().config());
        KafkaClusterStateParameters kafkaClusterStateParameters = new KafkaClusterStateParameters();
        try {
            kafkaClusterStateParameters.initParameters(verbose, topic, json);
            String outputString = json ? kafkaClusterState.getJSONString(kafkaClusterStateParameters) : kafkaClusterState.getPlaintext(kafkaClusterStateParameters);
            useUserTaskManager(context, kafkaClusterStateParameters, kafkaClusterState);
            context.response()
                    .setStatusCode(RESPONSE_OK)
                    .end(outputString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Operation(summary = "Returns Cruise Control State", method = "GET", operationId = "state",
            tags = {
                    "cruiseControlState"
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
    public void cruiseControlState(RoutingContext context) {
        MultiMap params = context.queryParams();
        boolean verbose = Boolean.parseBoolean(params.get(VERBOSE_PARAM));
        boolean superVerbose = Boolean.parseBoolean(params.get(SUPER_VERBOSE_PARAM));
        boolean json = Boolean.parseBoolean(params.get(JSON_PARAM));
        ArrayList<String> subtateStrings = params.get(SUBSTATES_PARAM) == null ? null : new ArrayList<>(Arrays.asList(params.get(SUBSTATES_PARAM).split(",")));
        Set<CruiseControlState.SubState> subStateSet = new HashSet<>();
        if (params.get(SUBSTATES_PARAM) != null) {
            for (String state : subtateStrings) {
                subStateSet.add(CruiseControlState.SubState.valueOf(state.toUpperCase(Locale.ROOT)));
            }
        }
        Set<CruiseControlState.SubState> substates = !subStateSet.isEmpty() ? subStateSet
                : new HashSet<>(Arrays.asList(CruiseControlState.SubState.values()));
        CruiseControlStateParameters cruiseControlStateParameters = new CruiseControlStateParameters();
        try {
            cruiseControlStateParameters.initParameters(json, substates, verbose, superVerbose);
            GetStateRunnable getStateRunnable = new GetStateRunnable(KafkaCruiseControlVertxApp.getVerticle().getKafkaCruiseControl(),
                    new OperationFuture("Get state"), cruiseControlStateParameters);
            CruiseControlState cruiseControlState1 = getStateRunnable.getResult();
            String outputString = json ? cruiseControlState1.getJSONString(cruiseControlStateParameters) : cruiseControlState1.getPlaintext(cruiseControlStateParameters);
            useUserTaskManager(context, cruiseControlStateParameters, cruiseControlState1);
            context.response()
                    .setStatusCode(RESPONSE_OK)
                    .end(outputString);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    @Operation(summary = "Once Cruise Control load Monitor shows it is in the RUNNING state, Users can use the following HTTP GET to get the cluster load.", method = "GET", operationId = "load",
            tags = {
                    "Load"
            },
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "start",
                            description = "Start time of the cluster load.", schema = @Schema(type = "long")),
                    @Parameter(in = ParameterIn.QUERY, name = "end",
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
    public void load(RoutingContext context) {
        MultiMap params = context.queryParams();
        boolean verbose = Boolean.parseBoolean(params.get(VERBOSE_PARAM));
        boolean json = Boolean.parseBoolean(params.get(JSON_PARAM));
        if (params.get(END_MS_PARAM) != null && params.get(START_MS_PARAM) != null) {
            throw new IllegalArgumentException("Parameter time and parameter end are mutually exclusive and should not be specified in the same request.");
        }
        Long end = params.get(END_MS_PARAM) == null ? (params.get(TIME_PARAM) == null ? System.currentTimeMillis() : Long.parseLong(params.get("time"))) : Long.parseLong(params.get("end"));
        Long start = params.get(START_MS_PARAM) == null ? DEFAULT_START_TIME_FOR_CLUSTER_MODEL : Long.parseLong(params.get(START_MS_PARAM));
        boolean allowCapacityEstimation = params.get(ALLOW_CAPACITY_ESTIMATION_PARAM) == null || Boolean.parseBoolean(params.get(ALLOW_CAPACITY_ESTIMATION_PARAM));
        boolean populateDiskInfo = params.get(POPULATE_DISK_INFO_PARAM) != null && Boolean.parseBoolean(params.get(POPULATE_DISK_INFO_PARAM));
        boolean capacityOnly = params.get(CAPACITY_ONLY_PARAM) != null && Boolean.parseBoolean(params.get(CAPACITY_ONLY_PARAM));
        ClusterLoadParameters clusterLoadParameters = new ClusterLoadParameters();
        try {
            clusterLoadParameters.initParameters(json, end, start, allowCapacityEstimation, populateDiskInfo, capacityOnly);
            LoadRunnable loadRunnable = new LoadRunnable(KafkaCruiseControlVertxApp.getVerticle().getKafkaCruiseControl(),
                    new OperationFuture("Get broker stats"), clusterLoadParameters);
            BrokerStats brokerStats = loadRunnable.getResult();
            String outputString = json ? brokerStats.getJSONString() : brokerStats.toString();
            useUserTaskManager(context, clusterLoadParameters, brokerStats);
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
                                            "{\"userTasks\":[{\"Status\":\"Completed\",\"UserTaskId\":\"32d279e0-6a1b-48ea-88f4-f1a6fe38a161\",\"StartMs\":\"1629536818003\",\"ClientIdentity\":\"[0:0:0:0:0:0:0:1]\",\"RequestURL\":\"GET /state\"},{\"Status\":\"Completed\",\"UserTaskId\":\"9009c120-0515-4870-b577-23cfe94d0f77\",\"StartMs\":\"1629536867061\",\"ClientIdentity\":\"[0:0:0:0:0:0:0:1]\",\"RequestURL\":\"GET /state\"},{\"Status\":\"Completed\",\"UserTaskId\":\"3873d13c-14d4-49e8-958e-06200ff59559\",\"StartMs\":\"1629537791780\",\"ClientIdentity\":\"[0:0:0:0:0:0:0:1]\",\"RequestURL\":\"GET /load\"},{\"Status\":\"Completed\",\"UserTaskId\":\"476927fb-0104-47a3-b9b1-3169b9585dc6\",\"StartMs\":\"1629537799869\",\"ClientIdentity\":\"[0:0:0:0:0:0:0:1]\",\"RequestURL\":\"GET /load?josn\\u003dtrue\"},{\"Status\":\"Completed\",\"UserTaskId\":\"debeb0c0-d6f5-47e0-87ff-ec8c6874cf9a\",\"StartMs\":\"1629537810556\",\"ClientIdentity\":\"[0:0:0:0:0:0:0:1]\",\"RequestURL\":\"GET /load?json\\u003dtrue\"}],\"version\":1}",
                                            implementation = KafkaClusterState.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Not found."),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error.")
            }
    )
    public void userTasks(RoutingContext context) {
        MultiMap params = context.queryParams();
        boolean json = Boolean.parseBoolean(params.get(JSON_PARAM));
        boolean fetchCompletedTask = Boolean.parseBoolean(params.get(FETCH_COMPLETED_TASK_PARAM));
        String userTaskIds = params.get(USER_TASK_IDS_PARAM) == null ? null : params.get(USER_TASK_IDS_PARAM);
        String clientIds = params.get(CLIENT_IDS_PARAM) == null ? null : params.get(CLIENT_IDS_PARAM);
        String entries = params.get(ENTRIES_PARAM) == null ? null : params.get(ENTRIES_PARAM);
        String endpoints = params.get(ENDPOINTS_PARAM) == null ? null : params.get(ENDPOINTS_PARAM);
        String types = params.get(TYPES_PARAM) == null ? null : params.get(TYPES_PARAM);
        UserTaskState userTaskState = new UserTaskState(KafkaCruiseControlVertxApp.getVerticle().getUserTaskManager().getAllUserTasks(),
                KafkaCruiseControlVertxApp.getVerticle().getKafkaCruiseControl().config());
        UserTasksParameters userTasksParameters = new UserTasksParameters();
        try {
            userTasksParameters.initParameters(json, userTaskIds, clientIds, endpoints, types, entries, fetchCompletedTask);
            String outputString = json ? userTaskState.getJSONString(userTasksParameters) : userTaskState.getPlaintext(userTasksParameters);
            useUserTaskManager(context, userTasksParameters, userTaskState);
            context.response()
                    .setStatusCode(RESPONSE_OK)
                    .end(outputString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Operation(summary = "Gives the partition load sorted by the utilization of a given resource.", method = "GET", operationId = "partition_load",
            tags = {
                    "PartitionLoad"
            },
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "resource",
                            description = "Resource type to sort partition load, available resources are DISK/CPU/NW_IN/NW_OUT.", schema = @Schema(type = "string")),
                    @Parameter(in = ParameterIn.QUERY, name = "start",
                            description = "The timestamp in millisecond of the earliest metric sample use to generate load.", schema = @Schema(type = "long")),
                    @Parameter(in = ParameterIn.QUERY, name = "end",
                            description = "The timestamp in millisecond of the latest metric sample use to generate load.", schema = @Schema(type = "long")),
                    @Parameter(in = ParameterIn.QUERY, name = "entries",
                            description = "Number of partition load entries to report in response.", schema = @Schema(type = "integer")),
                    @Parameter(in = ParameterIn.QUERY, name = "json",
                            description = "Return in JSON format or not.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "allow_capacity_estimation",
                            description = "Whether to allow capacity estimation when cruise-control is unable to obtain all per-broker capacity information.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "max_load",
                            description = "Whether report the max load for partition in windows.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "avg_load",
                            description = "Whether report the average load for partition in windows.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "topic",
                            description = "Regular expression to filter partition load to report based on partition's topic.", schema = @Schema(type = "regex")),
                    @Parameter(in = ParameterIn.QUERY, name = "partition",
                            description = "Partition number(e.g. 10) range(e.g. 1-10) to filter partition load to report.", schema = @Schema(type = "integer/range")),
                    @Parameter(in = ParameterIn.QUERY, name = "min_valid_partition_ratio",
                            description = "Minimal valid partition ratio requirement for cluster model.", schema = @Schema(type = "double")),
                    @Parameter(in = ParameterIn.QUERY, name = "brokerid",
                            description = "Broker id to to filter partition load to report.", schema = @Schema(type = "int")),
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
                                            "{\"records\":[{\"leader\":2,\"disk\":0.0381927490234375,\"partition\":0,\"followers\":[],\"msg_in\":4.770534992218018,\"topic\":\"__CruiseControlMetrics\",\"cpu\":0.08448822051286697,\"networkOutbound\":0.10010731220245361,\"networkInbound\":0.04455564171075821},{\"leader\":2,\"disk\":9.794235229492188E-4,\"partition\":30,\"followers\":[0],\"msg_in\":0.09136062115430832,\"topic\":\"__KafkaCruiseControlPartitionMetricSamples\",\"cpu\":0.0017388327978551388,\"networkOutbound\":0.00135068001691252,\"networkInbound\":0.00135068001691252},{\"leader\":2,\"disk\":9.355545043945312E-4,\"partition\":12,\"followers\":[0],\"msg_in\":0.09136062115430832,\"topic\":\"__KafkaCruiseControlPartitionMetricSamples\",\"cpu\":0.0017388327978551388,\"networkOutbound\":0.00135068001691252,\"networkInbound\":0.00135068001691252},{\"leader\":1,\"disk\":8.974075317382812E-4,\"partition\":13,\"followers\":[2],\"msg_in\":0.07918345928192139,\"topic\":\"__KafkaCruiseControlPartitionMetricSamples\",\"cpu\":0.0,\"networkOutbound\":0.0011194655671715736,\"networkInbound\":0.0011194655671715736},{\"leader\":0,\"disk\":8.85009765625E-4,\"partition\":29,\"followers\":[2],\"msg_in\":0.11304149031639099,\"topic\":\"__KafkaCruiseControlPartitionMetricSamples\",\"cpu\":0.0,\"networkOutbound\":0.001367346616461873,\"networkInbound\":0.001367346616461873},{\"leader\":0,\"disk\":7.59124755859375E-4,\"partition\":17,\"followers\":[2],\"msg_in\":0.11304149031639099,\"topic\":\"__KafkaCruiseControlPartitionMetricSamples\",\"cpu\":0.0,\"networkOutbound\":0.001367346616461873,\"networkInbound\":0.001367346616461873},{\"leader\":0,\"disk\":6.0272216796875E-4,\"partition\":5,\"followers\":[1],\"msg_in\":0.006292549427598715,\"topic\":\"__KafkaCruiseControlModelTrainingSamples\",\"cpu\":0.0,\"networkOutbound\":0.0012945608468726277,\"networkInbound\":0.0012945608468726277},{\"leader\":2,\"disk\":6.008148193359375E-4,\"partition\":30,\"followers\":[1],\"msg_in\":0.003375930478796363,\"topic\":\"__KafkaCruiseControlModelTrainingSamples\",\"cpu\":7.844195934012532E-4,\"networkOutbound\":5.690616089850664E-4,\"networkInbound\":5.690616089850664E-4},{\"leader\":2,\"disk\":5.884170532226562E-4,\"partition\":24,\"followers\":[1],\"msg_in\":0.003375930478796363,\"topic\":\"__KafkaCruiseControlModelTrainingSamples\",\"cpu\":7.844195934012532E-4,\"networkOutbound\":5.690616089850664E-4,\"networkInbound\":5.690616089850664E-4},{\"leader\":2,\"disk\":5.445480346679688E-4,\"partition\":0,\"followers\":[1],\"msg_in\":0.003375930478796363,\"topic\":\"__KafkaCruiseControlModelTrainingSamples\",\"cpu\":7.844195934012532E-4,\"networkOutbound\":5.690616089850664E-4,\"networkInbound\":5.690616089850664E-4},{\"leader\":2,\"disk\":4.711151123046875E-4,\"partition\":3,\"followers\":[0],\"msg_in\":0.003375930478796363,\"topic\":\"__KafkaCruiseControlModelTrainingSamples\",\"cpu\":7.844195934012532E-4,\"networkOutbound\":5.690616089850664E-4,\"networkInbound\":5.690616089850664E-4},{\"leader\":1,\"disk\":0.0,\"partition\":10,\"followers\":[2],\"msg_in\":0.0,\"topic\":\"__KafkaCruiseControlModelTrainingSamples\",\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.0},{\"leader\":1,\"disk\":0.0,\"partition\":16,\"followers\":[0],\"msg_in\":0.07918345928192139,\"topic\":\"__KafkaCruiseControlPartitionMetricSamples\",\"cpu\":0.0,\"networkOutbound\":0.0011194655671715736,\"networkInbound\":0.0011194655671715736},{\"leader\":0,\"disk\":0.0,\"partition\":14,\"followers\":[2],\"msg_in\":0.006292549427598715,\"topic\":\"__KafkaCruiseControlModelTrainingSamples\",\"cpu\":0.0,\"networkOutbound\":0.0012945608468726277,\"networkInbound\":0.0012945608468726277},{\"leader\":2,\"disk\":0.0,\"partition\":18,\"followers\":[1],\"msg_in\":0.003375930478796363,\"topic\":\"__KafkaCruiseControlModelTrainingSamples\",\"cpu\":7.844195934012532E-4,\"networkOutbound\":5.690616089850664E-4,\"networkInbound\":5.690616089850664E-4},{\"leader\":0,\"disk\":0.0,\"partition\":8,\"followers\":[1],\"msg_in\":0.11304149031639099,\"topic\":\"__KafkaCruiseControlPartitionMetricSamples\",\"cpu\":0.0,\"networkOutbound\":0.001367346616461873,\"networkInbound\":0.001367346616461873},{\"leader\":1,\"disk\":0.0,\"partition\":22,\"followers\":[2],\"msg_in\":0.0,\"topic\":\"__KafkaCruiseControlModelTrainingSamples\",\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.0},{\"leader\":1,\"disk\":0.0,\"partition\":4,\"followers\":[0],\"msg_in\":0.07918345928192139,\"topic\":\"__KafkaCruiseControlPartitionMetricSamples\",\"cpu\":0.0,\"networkOutbound\":0.0011194655671715736,\"networkInbound\":0.0011194655671715736},{\"leader\":0,\"disk\":0.0,\"partition\":26,\"followers\":[2],\"msg_in\":0.006292549427598715,\"topic\":\"__KafkaCruiseControlModelTrainingSamples\",\"cpu\":0.0,\"networkOutbound\":0.0012945608468726277,\"networkInbound\":0.0012945608468726277},{\"leader\":2,\"disk\":0.0,\"partition\":0,\"followers\":[0],\"msg_in\":0.09136062115430832,\"topic\":\"__KafkaCruiseControlPartitionMetricSamples\",\"cpu\":0.0017388327978551388,\"networkOutbound\":0.00135068001691252,\"networkInbound\":0.00135068001691252},{\"leader\":1,\"disk\":0.0,\"partition\":1,\"followers\":[0],\"msg_in\":0.0,\"topic\":\"__KafkaCruiseControlModelTrainingSamples\",\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.0},{\"leader\":1,\"disk\":0.0,\"partition\":25,\"followers\":[2],\"msg_in\":0.07918345928192139,\"topic\":\"__KafkaCruiseControlPartitionMetricSamples\",\"cpu\":0.0,\"networkOutbound\":0.0011194655671715736,\"networkInbound\":0.0011194655671715736},{\"leader\":2,\"disk\":0.0,\"partition\":21,\"followers\":[1],\"msg_in\":0.09136062115430832,\"topic\":\"__KafkaCruiseControlPartitionMetricSamples\",\"cpu\":0.0017388327978551388,\"networkOutbound\":0.00135068001691252,\"networkInbound\":0.00135068001691252},{\"leader\":2,\"disk\":0.0,\"partition\":9,\"followers\":[0],\"msg_in\":0.003375930478796363,\"topic\":\"__KafkaCruiseControlModelTrainingSamples\",\"cpu\":7.844195934012532E-4,\"networkOutbound\":5.690616089850664E-4,\"networkInbound\":5.690616089850664E-4},{\"leader\":1,\"disk\":0.0,\"partition\":13,\"followers\":[0],\"msg_in\":0.0,\"topic\":\"__KafkaCruiseControlModelTrainingSamples\",\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.0},{\"leader\":0,\"disk\":0.0,\"partition\":17,\"followers\":[1],\"msg_in\":0.006292549427598715,\"topic\":\"__KafkaCruiseControlModelTrainingSamples\",\"cpu\":0.0,\"networkOutbound\":0.0012945608468726277,\"networkInbound\":0.0012945608468726277},{\"leader\":2,\"disk\":0.0,\"partition\":9,\"followers\":[1],\"msg_in\":0.09136062115430832,\"topic\":\"__KafkaCruiseControlPartitionMetricSamples\",\"cpu\":0.0017388327978551388,\"networkOutbound\":0.00135068001691252,\"networkInbound\":0.00135068001691252},{\"leader\":2,\"disk\":0.0,\"partition\":21,\"followers\":[0],\"msg_in\":0.003375930478796363,\"topic\":\"__KafkaCruiseControlModelTrainingSamples\",\"cpu\":7.844195934012532E-4,\"networkOutbound\":5.690616089850664E-4,\"networkInbound\":5.690616089850664E-4},{\"leader\":0,\"disk\":0.0,\"partition\":5,\"followers\":[2],\"msg_in\":0.11304149031639099,\"topic\":\"__KafkaCruiseControlPartitionMetricSamples\",\"cpu\":0.0,\"networkOutbound\":0.001367346616461873,\"networkInbound\":0.001367346616461873},{\"leader\":1,\"disk\":0.0,\"partition\":25,\"followers\":[0],\"msg_in\":0.0,\"topic\":\"__KafkaCruiseControlModelTrainingSamples\",\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.0},{\"leader\":1,\"disk\":0.0,\"partition\":1,\"followers\":[2],\"msg_in\":0.07918345928192139,\"topic\":\"__KafkaCruiseControlPartitionMetricSamples\",\"cpu\":0.0,\"networkOutbound\":0.0011194655671715736,\"networkInbound\":0.0011194655671715736},{\"leader\":0,\"disk\":0.0,\"partition\":29,\"followers\":[1],\"msg_in\":0.006292549427598715,\"topic\":\"__KafkaCruiseControlModelTrainingSamples\",\"cpu\":0.0,\"networkOutbound\":0.0012945608468726277,\"networkInbound\":0.0012945608468726277},{\"leader\":1,\"disk\":0.0,\"partition\":4,\"followers\":[2],\"msg_in\":0.0,\"topic\":\"__KafkaCruiseControlModelTrainingSamples\",\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.0},{\"leader\":0,\"disk\":0.0,\"partition\":26,\"followers\":[1],\"msg_in\":0.11304149031639099,\"topic\":\"__KafkaCruiseControlPartitionMetricSamples\",\"cpu\":0.0,\"networkOutbound\":0.001367346616461873,\"networkInbound\":0.001367346616461873},{\"leader\":0,\"disk\":0.0,\"partition\":8,\"followers\":[2],\"msg_in\":0.006292549427598715,\"topic\":\"__KafkaCruiseControlModelTrainingSamples\",\"cpu\":0.0,\"networkOutbound\":0.0012945608468726277,\"networkInbound\":0.0012945608468726277},{\"leader\":1,\"disk\":0.0,\"partition\":22,\"followers\":[0],\"msg_in\":0.07918345928192139,\"topic\":\"__KafkaCruiseControlPartitionMetricSamples\",\"cpu\":0.0,\"networkOutbound\":0.0011194655671715736,\"networkInbound\":0.0011194655671715736},{\"leader\":2,\"disk\":0.0,\"partition\":12,\"followers\":[1],\"msg_in\":0.003375930478796363,\"topic\":\"__KafkaCruiseControlModelTrainingSamples\",\"cpu\":7.844195934012532E-4,\"networkOutbound\":5.690616089850664E-4,\"networkInbound\":5.690616089850664E-4},{\"leader\":2,\"disk\":0.0,\"partition\":18,\"followers\":[0],\"msg_in\":0.09136062115430832,\"topic\":\"__KafkaCruiseControlPartitionMetricSamples\",\"cpu\":0.0017388327978551388,\"networkOutbound\":0.00135068001691252,\"networkInbound\":0.00135068001691252},{\"leader\":1,\"disk\":0.0,\"partition\":16,\"followers\":[2],\"msg_in\":0.0,\"topic\":\"__KafkaCruiseControlModelTrainingSamples\",\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.0},{\"leader\":0,\"disk\":0.0,\"partition\":14,\"followers\":[1],\"msg_in\":0.11304149031639099,\"topic\":\"__KafkaCruiseControlPartitionMetricSamples\",\"cpu\":0.0,\"networkOutbound\":0.001367346616461873,\"networkInbound\":0.001367346616461873},{\"leader\":0,\"disk\":0.0,\"partition\":20,\"followers\":[2],\"msg_in\":0.006292549427598715,\"topic\":\"__KafkaCruiseControlModelTrainingSamples\",\"cpu\":0.0,\"networkOutbound\":0.0012945608468726277,\"networkInbound\":0.0012945608468726277},{\"leader\":1,\"disk\":0.0,\"partition\":10,\"followers\":[0],\"msg_in\":0.07918345928192139,\"topic\":\"__KafkaCruiseControlPartitionMetricSamples\",\"cpu\":0.0,\"networkOutbound\":0.0011194655671715736,\"networkInbound\":0.0011194655671715736},{\"leader\":2,\"disk\":0.0,\"partition\":6,\"followers\":[0],\"msg_in\":0.09136062115430832,\"topic\":\"__KafkaCruiseControlPartitionMetricSamples\",\"cpu\":0.0017388327978551388,\"networkOutbound\":0.00135068001691252,\"networkInbound\":0.00135068001691252},{\"leader\":1,\"disk\":0.0,\"partition\":28,\"followers\":[2],\"msg_in\":0.0,\"topic\":\"__KafkaCruiseControlModelTrainingSamples\",\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.0},{\"leader\":0,\"disk\":0.0,\"partition\":2,\"followers\":[1],\"msg_in\":0.11304149031639099,\"topic\":\"__KafkaCruiseControlPartitionMetricSamples\",\"cpu\":0.0,\"networkOutbound\":0.001367346616461873,\"networkInbound\":0.001367346616461873},{\"leader\":1,\"disk\":0.0,\"partition\":31,\"followers\":[2],\"msg_in\":0.07918345928192139,\"topic\":\"__KafkaCruiseControlPartitionMetricSamples\",\"cpu\":0.0,\"networkOutbound\":0.0011194655671715736,\"networkInbound\":0.0011194655671715736},{\"leader\":2,\"disk\":0.0,\"partition\":27,\"followers\":[1],\"msg_in\":0.09136062115430832,\"topic\":\"__KafkaCruiseControlPartitionMetricSamples\",\"cpu\":0.0017388327978551388,\"networkOutbound\":0.00135068001691252,\"networkInbound\":0.00135068001691252},{\"leader\":0,\"disk\":0.0,\"partition\":23,\"followers\":[2],\"msg_in\":0.11304149031639099,\"topic\":\"__KafkaCruiseControlPartitionMetricSamples\",\"cpu\":0.0,\"networkOutbound\":0.001367346616461873,\"networkInbound\":0.001367346616461873},{\"leader\":1,\"disk\":0.0,\"partition\":19,\"followers\":[2],\"msg_in\":0.07918345928192139,\"topic\":\"__KafkaCruiseControlPartitionMetricSamples\",\"cpu\":0.0,\"networkOutbound\":0.0011194655671715736,\"networkInbound\":0.0011194655671715736},{\"leader\":1,\"disk\":0.0,\"partition\":7,\"followers\":[0],\"msg_in\":0.0,\"topic\":\"__KafkaCruiseControlModelTrainingSamples\",\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.0},{\"leader\":2,\"disk\":0.0,\"partition\":15,\"followers\":[1],\"msg_in\":0.09136062115430832,\"topic\":\"__KafkaCruiseControlPartitionMetricSamples\",\"cpu\":0.0017388327978551388,\"networkOutbound\":0.00135068001691252,\"networkInbound\":0.00135068001691252},{\"leader\":0,\"disk\":0.0,\"partition\":11,\"followers\":[1],\"msg_in\":0.006292549427598715,\"topic\":\"__KafkaCruiseControlModelTrainingSamples\",\"cpu\":0.0,\"networkOutbound\":0.0012945608468726277,\"networkInbound\":0.0012945608468726277},{\"leader\":0,\"disk\":0.0,\"partition\":11,\"followers\":[2],\"msg_in\":0.11304149031639099,\"topic\":\"__KafkaCruiseControlPartitionMetricSamples\",\"cpu\":0.0,\"networkOutbound\":0.001367346616461873,\"networkInbound\":0.001367346616461873},{\"leader\":2,\"disk\":0.0,\"partition\":15,\"followers\":[0],\"msg_in\":0.003375930478796363,\"topic\":\"__KafkaCruiseControlModelTrainingSamples\",\"cpu\":7.844195934012532E-4,\"networkOutbound\":5.690616089850664E-4,\"networkInbound\":5.690616089850664E-4},{\"leader\":1,\"disk\":0.0,\"partition\":7,\"followers\":[2],\"msg_in\":0.07918345928192139,\"topic\":\"__KafkaCruiseControlPartitionMetricSamples\",\"cpu\":0.0,\"networkOutbound\":0.0011194655671715736,\"networkInbound\":0.0011194655671715736},{\"leader\":1,\"disk\":0.0,\"partition\":19,\"followers\":[0],\"msg_in\":0.0,\"topic\":\"__KafkaCruiseControlModelTrainingSamples\",\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.0},{\"leader\":2,\"disk\":0.0,\"partition\":3,\"followers\":[1],\"msg_in\":0.09136062115430832,\"topic\":\"__KafkaCruiseControlPartitionMetricSamples\",\"cpu\":0.0017388327978551388,\"networkOutbound\":0.00135068001691252,\"networkInbound\":0.00135068001691252},{\"leader\":0,\"disk\":0.0,\"partition\":23,\"followers\":[1],\"msg_in\":0.006292549427598715,\"topic\":\"__KafkaCruiseControlModelTrainingSamples\",\"cpu\":0.0,\"networkOutbound\":0.0012945608468726277,\"networkInbound\":0.0012945608468726277},{\"leader\":2,\"disk\":0.0,\"partition\":27,\"followers\":[0],\"msg_in\":0.003375930478796363,\"topic\":\"__KafkaCruiseControlModelTrainingSamples\",\"cpu\":7.844195934012532E-4,\"networkOutbound\":5.690616089850664E-4,\"networkInbound\":5.690616089850664E-4},{\"leader\":1,\"disk\":0.0,\"partition\":28,\"followers\":[0],\"msg_in\":0.07918345928192139,\"topic\":\"__KafkaCruiseControlPartitionMetricSamples\",\"cpu\":0.0,\"networkOutbound\":0.0011194655671715736,\"networkInbound\":0.0011194655671715736},{\"leader\":1,\"disk\":0.0,\"partition\":31,\"followers\":[0],\"msg_in\":0.0,\"topic\":\"__KafkaCruiseControlModelTrainingSamples\",\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.0},{\"leader\":0,\"disk\":0.0,\"partition\":2,\"followers\":[2],\"msg_in\":0.006292549427598715,\"topic\":\"__KafkaCruiseControlModelTrainingSamples\",\"cpu\":0.0,\"networkOutbound\":0.0012945608468726277,\"networkInbound\":0.0012945608468726277},{\"leader\":2,\"disk\":0.0,\"partition\":24,\"followers\":[0],\"msg_in\":0.09136062115430832,\"topic\":\"__KafkaCruiseControlPartitionMetricSamples\",\"cpu\":0.0017388327978551388,\"networkOutbound\":0.00135068001691252,\"networkInbound\":0.00135068001691252},{\"leader\":2,\"disk\":0.0,\"partition\":6,\"followers\":[1],\"msg_in\":0.003375930478796363,\"topic\":\"__KafkaCruiseControlModelTrainingSamples\",\"cpu\":7.844195934012532E-4,\"networkOutbound\":5.690616089850664E-4,\"networkInbound\":5.690616089850664E-4},{\"leader\":0,\"disk\":0.0,\"partition\":20,\"followers\":[1],\"msg_in\":0.11304149031639099,\"topic\":\"__KafkaCruiseControlPartitionMetricSamples\",\"cpu\":0.0,\"networkOutbound\":0.001367346616461873,\"networkInbound\":0.001367346616461873}],\"version\":1}",
                                            implementation = KafkaClusterState.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Not found."),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error.")
            }
    )

    public void partitionLoad(RoutingContext context) {
        MultiMap params = context.queryParams();
        String resource = params.get(RESOURCE_PARAM) == null ? "DISK" : params.get(RESOURCE_PARAM);
        Long start = params.get(START_MS_PARAM) == null ? DEFAULT_START_TIME_FOR_CLUSTER_MODEL : Long.parseLong(params.get(START_MS_PARAM));
        Long end = params.get(END_MS_PARAM) == null ? (params.get(TIME_PARAM) == null ? System.currentTimeMillis() : Long.parseLong(params.get("time"))) : Long.parseLong(params.get("end"));
        Integer entries = Integer.parseInt(params.get(ENTRIES_PARAM) == null ? String.valueOf(Integer.MAX_VALUE) : params.get(ENTRIES_PARAM));
        boolean json = Boolean.parseBoolean(params.get(JSON_PARAM));
        boolean allowCapacityEstimation = params.get(ALLOW_CAPACITY_ESTIMATION_PARAM) == null || Boolean.parseBoolean(params.get(ALLOW_CAPACITY_ESTIMATION_PARAM));
        boolean maxLoad = Boolean.parseBoolean(params.get(MAX_LOAD_PARAM));
        boolean avgLoad = Boolean.parseBoolean(params.get(AVG_LOAD_PARAM));
        Pattern topic = Pattern.compile(params.get(TOPIC_PARAM) == null ? ".*" : params.get(TOPIC_PARAM));
        String partition = params.get(PARTITION_PARAM);
        Double minValidPartitionRatio = params.get(MIN_VALID_PARTITION_RATIO_PARAM) == null ? null : Double.parseDouble(params.get(MIN_VALID_PARTITION_RATIO_PARAM));
        String brokerid = params.get(BROKER_ID_PARAM);

        PartitionLoadParameters partitionLoadParameters = new PartitionLoadParameters();
        try {
            partitionLoadParameters.initParameters(json, maxLoad, avgLoad, topic, partition, entries,
                    minValidPartitionRatio, allowCapacityEstimation, brokerid, start, end, resource);
            PartitionLoadRunnable partitionLoadRunnable = new PartitionLoadRunnable(KafkaCruiseControlVertxApp.getVerticle().getKafkaCruiseControl(),
                    new OperationFuture(String.format("Get partition load from %d to %d", start, end)), partitionLoadParameters);
            PartitionLoadState partitionLoadState = partitionLoadRunnable.getResult();
            useUserTaskManager(context, partitionLoadParameters, partitionLoadState);
            String outputString = json ? partitionLoadState.getJSONString() : partitionLoadState.getPlaintext();
            context.response()
                    .setStatusCode(RESPONSE_OK)
                    .end(outputString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Operation(summary = "Returns the optimization proposals generated based on the workload model of the given timestamp. The workload summary before and after the optimization will also be returned.", method = "GET", operationId = "proposals",
            tags = {
                    "Proposals"
            },
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "ignore_proposal_cache",
                            description = "Whether to ignore the cached proposal or not.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "data_from",
                            description = "Whether to calculate proposal from available valid partitions or valid windows.", schema = @Schema(type = "string")),
                    @Parameter(in = ParameterIn.QUERY, name = "goals",
                            description = "List of goals used to generate proposal.", schema = @Schema(type = "list")),
                    @Parameter(in = ParameterIn.QUERY, name = "kafka_assigner",
                            description = "Whether to use Kafka assigner mode to generate proposals.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "allow_capacity_estimation",
                            description = "Whether to allow broker capacity to be estimated.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "excluded_topics",
                            description = "Regular expression to specify topics excluded from replica and leadership movement.", schema = @Schema(type = "regex")),
                    @Parameter(in = ParameterIn.QUERY, name = "use_ready_default_goals",
                            description = "Whether to use only ready goals to generate proposals.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "exclude_recently_demoted_brokers",
                            description = "Whether to allow leader replicas to be moved to recently demoted brokers.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "exclude_recently_removed_brokers",
                            description = "Whether to allow leader replicas to be moved to recently removed broker.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "destination_broker_ids",
                            description = "Specify brokers to move replicas to.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "rebalance_disk",
                            description = "Whether to balance load between disks within brokers (requires JBOD Kafka deployment).", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "json",
                            description = "Return in JSON format or not.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "verbose",
                            description = "Return detailed state information.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "doAs",
                            description = "Propagated user by the trusted proxy service.", schema = @Schema(type = "string")),
                    @Parameter(in = ParameterIn.QUERY, name = "fast_mode",
                            description = "True to compute proposals in fast mode, false otherwise.", schema = @Schema(type = "boolean"))

            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    encoding = @Encoding(contentType = "application/json"),
                                    schema = @Schema(name = "jsonState", example =
                                            "{\"summary\":{\"numIntraBrokerReplicaMovements\":0,\"numReplicaMovements\":0,\"onDemandBalancednessScoreAfter\":93.21842452909542,\"intraBrokerDataToMoveMB\":0,\"monitoredPartitionsPercentage\":100.0,\"provisionRecommendation\":\"\",\"excludedBrokersForReplicaMove\":[],\"excludedBrokersForLeadership\":[],\"provisionStatus\":\"RIGHT_SIZED\",\"onDemandBalancednessScoreBefore\":93.21842452909542,\"recentWindows\":1,\"dataToMoveMB\":0,\"excludedTopics\":[],\"numLeaderMovements\":4},\"goalSummary\":[{\"optimizationTimeMs\":23,\"clusterModelStats\":{\"metadata\":{\"replicas\":129,\"topics\":3,\"brokers\":3},\"statistics\":{\"AVG\":{\"disk\":0.0022366841634114585,\"replicas\":43.0,\"leaderReplicas\":21.666666666666668,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.012441294888655344,\"topicReplicas\":14.333333333333334,\"potentialNwOut\":0.0},\"STD\":{\"disk\":0.003163149078641604,\"replicas\":0.816496580927726,\"leaderReplicas\":1.247219128924647,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.0,\"topicReplicas\":0.4714045207910316,\"potentialNwOut\":0.0},\"MIN\":{\"disk\":0.0,\"replicas\":42,\"leaderReplicas\":20,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.037323884665966034,\"topicReplicas\":0,\"potentialNwOut\":0.0},\"MAX\":{\"disk\":0.006710052490234375,\"replicas\":44,\"leaderReplicas\":23,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.037323884665966034,\"topicReplicas\":22,\"potentialNwOut\":0.0}}},\"goal\":\"RackAwareDistributionGoal\",\"status\":\"NO-ACTION\"},{\"optimizationTimeMs\":2,\"clusterModelStats\":{\"metadata\":{\"replicas\":129,\"topics\":3,\"brokers\":3},\"statistics\":{\"AVG\":{\"disk\":0.0022366841634114585,\"replicas\":43.0,\"leaderReplicas\":21.666666666666668,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.012441294888655344,\"topicReplicas\":14.333333333333334,\"potentialNwOut\":0.0},\"STD\":{\"disk\":0.003163149078641604,\"replicas\":0.816496580927726,\"leaderReplicas\":1.247219128924647,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.0,\"topicReplicas\":0.4714045207910316,\"potentialNwOut\":0.0},\"MIN\":{\"disk\":0.0,\"replicas\":42,\"leaderReplicas\":20,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.037323884665966034,\"topicReplicas\":0,\"potentialNwOut\":0.0},\"MAX\":{\"disk\":0.006710052490234375,\"replicas\":44,\"leaderReplicas\":23,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.037323884665966034,\"topicReplicas\":22,\"potentialNwOut\":0.0}}},\"goal\":\"ReplicaCapacityGoal\",\"status\":\"NO-ACTION\"},{\"optimizationTimeMs\":1,\"clusterModelStats\":{\"metadata\":{\"replicas\":129,\"topics\":3,\"brokers\":3},\"statistics\":{\"AVG\":{\"disk\":0.0022366841634114585,\"replicas\":43.0,\"leaderReplicas\":21.666666666666668,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.012441294888655344,\"topicReplicas\":14.333333333333334,\"potentialNwOut\":0.0},\"STD\":{\"disk\":0.003163149078641604,\"replicas\":0.816496580927726,\"leaderReplicas\":1.247219128924647,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.0,\"topicReplicas\":0.4714045207910316,\"potentialNwOut\":0.0},\"MIN\":{\"disk\":0.0,\"replicas\":42,\"leaderReplicas\":20,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.037323884665966034,\"topicReplicas\":0,\"potentialNwOut\":0.0},\"MAX\":{\"disk\":0.006710052490234375,\"replicas\":44,\"leaderReplicas\":23,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.037323884665966034,\"topicReplicas\":22,\"potentialNwOut\":0.0}}},\"goal\":\"DiskCapacityGoal\",\"status\":\"NO-ACTION\"},{\"optimizationTimeMs\":1,\"clusterModelStats\":{\"metadata\":{\"replicas\":129,\"topics\":3,\"brokers\":3},\"statistics\":{\"AVG\":{\"disk\":0.0022366841634114585,\"replicas\":43.0,\"leaderReplicas\":21.666666666666668,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.012441294888655344,\"topicReplicas\":14.333333333333334,\"potentialNwOut\":0.0},\"STD\":{\"disk\":0.003163149078641604,\"replicas\":0.816496580927726,\"leaderReplicas\":1.247219128924647,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.0,\"topicReplicas\":0.4714045207910316,\"potentialNwOut\":0.0},\"MIN\":{\"disk\":0.0,\"replicas\":42,\"leaderReplicas\":20,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.037323884665966034,\"topicReplicas\":0,\"potentialNwOut\":0.0},\"MAX\":{\"disk\":0.006710052490234375,\"replicas\":44,\"leaderReplicas\":23,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.037323884665966034,\"topicReplicas\":22,\"potentialNwOut\":0.0}}},\"goal\":\"NetworkInboundCapacityGoal\",\"status\":\"NO-ACTION\"},{\"optimizationTimeMs\":0,\"clusterModelStats\":{\"metadata\":{\"replicas\":129,\"topics\":3,\"brokers\":3},\"statistics\":{\"AVG\":{\"disk\":0.0022366841634114585,\"replicas\":43.0,\"leaderReplicas\":21.666666666666668,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.012441294888655344,\"topicReplicas\":14.333333333333334,\"potentialNwOut\":0.0},\"STD\":{\"disk\":0.003163149078641604,\"replicas\":0.816496580927726,\"leaderReplicas\":1.247219128924647,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.0,\"topicReplicas\":0.4714045207910316,\"potentialNwOut\":0.0},\"MIN\":{\"disk\":0.0,\"replicas\":42,\"leaderReplicas\":20,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.037323884665966034,\"topicReplicas\":0,\"potentialNwOut\":0.0},\"MAX\":{\"disk\":0.006710052490234375,\"replicas\":44,\"leaderReplicas\":23,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.037323884665966034,\"topicReplicas\":22,\"potentialNwOut\":0.0}}},\"goal\":\"NetworkOutboundCapacityGoal\",\"status\":\"NO-ACTION\"},{\"optimizationTimeMs\":0,\"clusterModelStats\":{\"metadata\":{\"replicas\":129,\"topics\":3,\"brokers\":3},\"statistics\":{\"AVG\":{\"disk\":0.0022366841634114585,\"replicas\":43.0,\"leaderReplicas\":21.666666666666668,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.012441294888655344,\"topicReplicas\":14.333333333333334,\"potentialNwOut\":0.0},\"STD\":{\"disk\":0.003163149078641604,\"replicas\":0.816496580927726,\"leaderReplicas\":1.247219128924647,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.0,\"topicReplicas\":0.4714045207910316,\"potentialNwOut\":0.0},\"MIN\":{\"disk\":0.0,\"replicas\":42,\"leaderReplicas\":20,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.037323884665966034,\"topicReplicas\":0,\"potentialNwOut\":0.0},\"MAX\":{\"disk\":0.006710052490234375,\"replicas\":44,\"leaderReplicas\":23,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.037323884665966034,\"topicReplicas\":22,\"potentialNwOut\":0.0}}},\"goal\":\"CpuCapacityGoal\",\"status\":\"NO-ACTION\"},{\"optimizationTimeMs\":3,\"clusterModelStats\":{\"metadata\":{\"replicas\":129,\"topics\":3,\"brokers\":3},\"statistics\":{\"AVG\":{\"disk\":0.0022366841634114585,\"replicas\":43.0,\"leaderReplicas\":21.666666666666668,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.012441294888655344,\"topicReplicas\":14.333333333333334,\"potentialNwOut\":0.0},\"STD\":{\"disk\":0.003163149078641604,\"replicas\":0.816496580927726,\"leaderReplicas\":1.247219128924647,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.0,\"topicReplicas\":0.4714045207910316,\"potentialNwOut\":0.0},\"MIN\":{\"disk\":0.0,\"replicas\":42,\"leaderReplicas\":20,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.037323884665966034,\"topicReplicas\":0,\"potentialNwOut\":0.0},\"MAX\":{\"disk\":0.006710052490234375,\"replicas\":44,\"leaderReplicas\":23,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.037323884665966034,\"topicReplicas\":22,\"potentialNwOut\":0.0}}},\"goal\":\"ReplicaDistributionGoal\",\"status\":\"NO-ACTION\"},{\"optimizationTimeMs\":1,\"clusterModelStats\":{\"metadata\":{\"replicas\":129,\"topics\":3,\"brokers\":3},\"statistics\":{\"AVG\":{\"disk\":0.0022366841634114585,\"replicas\":43.0,\"leaderReplicas\":21.666666666666668,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.012441294888655344,\"topicReplicas\":14.333333333333334,\"potentialNwOut\":0.0},\"STD\":{\"disk\":0.003163149078641604,\"replicas\":0.816496580927726,\"leaderReplicas\":1.247219128924647,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.0,\"topicReplicas\":0.4714045207910316,\"potentialNwOut\":0.0},\"MIN\":{\"disk\":0.0,\"replicas\":42,\"leaderReplicas\":20,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.037323884665966034,\"topicReplicas\":0,\"potentialNwOut\":0.0},\"MAX\":{\"disk\":0.006710052490234375,\"replicas\":44,\"leaderReplicas\":23,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.037323884665966034,\"topicReplicas\":22,\"potentialNwOut\":0.0}}},\"goal\":\"PotentialNwOutGoal\",\"status\":\"NO-ACTION\"},{\"optimizationTimeMs\":11,\"clusterModelStats\":{\"metadata\":{\"replicas\":129,\"topics\":3,\"brokers\":3},\"statistics\":{\"AVG\":{\"disk\":0.0022366841634114585,\"replicas\":43.0,\"leaderReplicas\":21.666666666666668,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.012441294888655344,\"topicReplicas\":14.333333333333334,\"potentialNwOut\":0.0},\"STD\":{\"disk\":0.003163149078641604,\"replicas\":0.816496580927726,\"leaderReplicas\":1.247219128924647,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.0,\"topicReplicas\":0.4714045207910316,\"potentialNwOut\":0.0},\"MIN\":{\"disk\":0.0,\"replicas\":42,\"leaderReplicas\":20,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.037323884665966034,\"topicReplicas\":0,\"potentialNwOut\":0.0},\"MAX\":{\"disk\":0.006710052490234375,\"replicas\":44,\"leaderReplicas\":23,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.037323884665966034,\"topicReplicas\":22,\"potentialNwOut\":0.0}}},\"goal\":\"DiskUsageDistributionGoal\",\"status\":\"VIOLATED\"},{\"optimizationTimeMs\":1,\"clusterModelStats\":{\"metadata\":{\"replicas\":129,\"topics\":3,\"brokers\":3},\"statistics\":{\"AVG\":{\"disk\":0.0022366841634114585,\"replicas\":43.0,\"leaderReplicas\":21.666666666666668,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.012441294888655344,\"topicReplicas\":14.333333333333334,\"potentialNwOut\":0.0},\"STD\":{\"disk\":0.003163149078641604,\"replicas\":0.816496580927726,\"leaderReplicas\":1.247219128924647,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.0,\"topicReplicas\":0.4714045207910316,\"potentialNwOut\":0.0},\"MIN\":{\"disk\":0.0,\"replicas\":42,\"leaderReplicas\":20,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.037323884665966034,\"topicReplicas\":0,\"potentialNwOut\":0.0},\"MAX\":{\"disk\":0.006710052490234375,\"replicas\":44,\"leaderReplicas\":23,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.037323884665966034,\"topicReplicas\":22,\"potentialNwOut\":0.0}}},\"goal\":\"NetworkInboundUsageDistributionGoal\",\"status\":\"NO-ACTION\"},{\"optimizationTimeMs\":1,\"clusterModelStats\":{\"metadata\":{\"replicas\":129,\"topics\":3,\"brokers\":3},\"statistics\":{\"AVG\":{\"disk\":0.0022366841634114585,\"replicas\":43.0,\"leaderReplicas\":21.666666666666668,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.012441294888655344,\"topicReplicas\":14.333333333333334,\"potentialNwOut\":0.0},\"STD\":{\"disk\":0.003163149078641604,\"replicas\":0.816496580927726,\"leaderReplicas\":1.247219128924647,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.0,\"topicReplicas\":0.4714045207910316,\"potentialNwOut\":0.0},\"MIN\":{\"disk\":0.0,\"replicas\":42,\"leaderReplicas\":20,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.037323884665966034,\"topicReplicas\":0,\"potentialNwOut\":0.0},\"MAX\":{\"disk\":0.006710052490234375,\"replicas\":44,\"leaderReplicas\":23,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.037323884665966034,\"topicReplicas\":22,\"potentialNwOut\":0.0}}},\"goal\":\"NetworkOutboundUsageDistributionGoal\",\"status\":\"NO-ACTION\"},{\"optimizationTimeMs\":0,\"clusterModelStats\":{\"metadata\":{\"replicas\":129,\"topics\":3,\"brokers\":3},\"statistics\":{\"AVG\":{\"disk\":0.0022366841634114585,\"replicas\":43.0,\"leaderReplicas\":21.666666666666668,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.012441294888655344,\"topicReplicas\":14.333333333333334,\"potentialNwOut\":0.0},\"STD\":{\"disk\":0.003163149078641604,\"replicas\":0.816496580927726,\"leaderReplicas\":1.247219128924647,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.0,\"topicReplicas\":0.4714045207910316,\"potentialNwOut\":0.0},\"MIN\":{\"disk\":0.0,\"replicas\":42,\"leaderReplicas\":20,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.037323884665966034,\"topicReplicas\":0,\"potentialNwOut\":0.0},\"MAX\":{\"disk\":0.006710052490234375,\"replicas\":44,\"leaderReplicas\":23,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.037323884665966034,\"topicReplicas\":22,\"potentialNwOut\":0.0}}},\"goal\":\"CpuUsageDistributionGoal\",\"status\":\"NO-ACTION\"},{\"optimizationTimeMs\":1,\"clusterModelStats\":{\"metadata\":{\"replicas\":129,\"topics\":3,\"brokers\":3},\"statistics\":{\"AVG\":{\"disk\":0.0022366841634114585,\"replicas\":43.0,\"leaderReplicas\":21.666666666666668,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.012441294888655344,\"topicReplicas\":14.333333333333334,\"potentialNwOut\":0.0},\"STD\":{\"disk\":0.003163149078641604,\"replicas\":0.816496580927726,\"leaderReplicas\":1.247219128924647,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.0,\"topicReplicas\":0.4714045207910316,\"potentialNwOut\":0.0},\"MIN\":{\"disk\":0.0,\"replicas\":42,\"leaderReplicas\":20,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.037323884665966034,\"topicReplicas\":0,\"potentialNwOut\":0.0},\"MAX\":{\"disk\":0.006710052490234375,\"replicas\":44,\"leaderReplicas\":23,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.037323884665966034,\"topicReplicas\":22,\"potentialNwOut\":0.0}}},\"goal\":\"TopicReplicaDistributionGoal\",\"status\":\"NO-ACTION\"},{\"optimizationTimeMs\":1,\"clusterModelStats\":{\"metadata\":{\"replicas\":129,\"topics\":3,\"brokers\":3},\"statistics\":{\"AVG\":{\"disk\":0.0022366841634114585,\"replicas\":43.0,\"leaderReplicas\":21.666666666666668,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.012441294888655344,\"topicReplicas\":14.333333333333334,\"potentialNwOut\":0.0},\"STD\":{\"disk\":0.003163149078641604,\"replicas\":0.816496580927726,\"leaderReplicas\":1.247219128924647,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.0,\"topicReplicas\":0.4714045207910316,\"potentialNwOut\":0.0},\"MIN\":{\"disk\":0.0,\"replicas\":42,\"leaderReplicas\":20,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.037323884665966034,\"topicReplicas\":0,\"potentialNwOut\":0.0},\"MAX\":{\"disk\":0.006710052490234375,\"replicas\":44,\"leaderReplicas\":23,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.037323884665966034,\"topicReplicas\":22,\"potentialNwOut\":0.0}}},\"goal\":\"LeaderReplicaDistributionGoal\",\"status\":\"NO-ACTION\"},{\"optimizationTimeMs\":11,\"clusterModelStats\":{\"metadata\":{\"replicas\":129,\"topics\":3,\"brokers\":3},\"statistics\":{\"AVG\":{\"disk\":0.0022366841634114585,\"replicas\":43.0,\"leaderReplicas\":21.666666666666668,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.012441294888655344,\"topicReplicas\":14.333333333333334,\"potentialNwOut\":0.0},\"STD\":{\"disk\":0.003163149078641604,\"replicas\":0.816496580927726,\"leaderReplicas\":2.0548046676563256,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.0,\"topicReplicas\":0.4714045207910316,\"potentialNwOut\":0.0},\"MIN\":{\"disk\":0.0,\"replicas\":42,\"leaderReplicas\":19,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.037323884665966034,\"topicReplicas\":0,\"potentialNwOut\":0.0},\"MAX\":{\"disk\":0.006710052490234375,\"replicas\":44,\"leaderReplicas\":24,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.037323884665966034,\"topicReplicas\":22,\"potentialNwOut\":0.0}}},\"goal\":\"LeaderBytesInDistributionGoal\",\"status\":\"VIOLATED\"}],\"loadAfterOptimization\":{\"hosts\":[{\"FollowerNwInRate\":0.0,\"NwOutRate\":0.0,\"NumCore\":3,\"Host\":\"192.168.31.136\",\"CpuPct\":0.0,\"Replicas\":129,\"NetworkInCapacity\":110000.0,\"Rack\":\"192.168.31.136\",\"Leaders\":65,\"DiskCapacityMB\":1500000.0,\"DiskMB\":0.006710052490234375,\"PnwOutRate\":0.0,\"NetworkOutCapacity\":110000.0,\"LeaderNwInRate\":0.037323884665966034,\"DiskPct\":4.473368326822917E-7}],\"brokers\":[{\"FollowerNwInRate\":0.0,\"BrokerState\":\"ALIVE\",\"Broker\":0,\"NwOutRate\":0.0,\"NumCore\":1,\"Host\":\"192.168.31.136\",\"CpuPct\":0.0,\"Replicas\":42,\"NetworkInCapacity\":50000.0,\"Rack\":\"192.168.31.136\",\"Leaders\":22,\"DiskCapacityMB\":500000.0,\"DiskMB\":0.0,\"PnwOutRate\":0.0,\"NetworkOutCapacity\":50000.0,\"LeaderNwInRate\":0.0,\"DiskPct\":0.0},{\"FollowerNwInRate\":0.0,\"BrokerState\":\"ALIVE\",\"Broker\":1,\"NwOutRate\":0.0,\"NumCore\":1,\"Host\":\"192.168.31.136\",\"CpuPct\":0.0,\"Replicas\":43,\"NetworkInCapacity\":50000.0,\"Rack\":\"192.168.31.136\",\"Leaders\":24,\"DiskCapacityMB\":500000.0,\"DiskMB\":0.0,\"PnwOutRate\":0.0,\"NetworkOutCapacity\":50000.0,\"LeaderNwInRate\":0.0,\"DiskPct\":0.0},{\"FollowerNwInRate\":0.0,\"BrokerState\":\"ALIVE\",\"Broker\":2,\"NwOutRate\":0.0,\"NumCore\":1,\"Host\":\"192.168.31.136\",\"CpuPct\":0.0,\"Replicas\":44,\"NetworkInCapacity\":10000.0,\"Rack\":\"192.168.31.136\",\"Leaders\":19,\"DiskCapacityMB\":500000.0,\"DiskMB\":0.006710052490234375,\"PnwOutRate\":0.0,\"NetworkOutCapacity\":10000.0,\"LeaderNwInRate\":0.037323884665966034,\"DiskPct\":1.342010498046875E-6}]},\"version\":1}",
                                            implementation = KafkaClusterState.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Not found."),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error.")
            }
    )
    public void proposals(RoutingContext context) {
        MultiMap params = context.queryParams();
        boolean ignoreProposalCache = Boolean.parseBoolean(params.get(IGNORE_PROPOSAL_CACHE_PARAM));
        String dataFrom = params.get(DATA_FROM_PARAM) == null ? "VALID_WINDOWS" : params.get(DATA_FROM_PARAM);
        String goals = params.get(GOALS_PARAM);
        boolean kafkaAssigner = Boolean.parseBoolean(params.get(KAFKA_ASSIGNER_MODE_PARAM));
        boolean allowCapacityEstimation = params.get(ALLOW_CAPACITY_ESTIMATION_PARAM) == null || Boolean.parseBoolean(params.get(ALLOW_CAPACITY_ESTIMATION_PARAM));
        Pattern excludedTopics = params.get(EXCLUDED_TOPICS_PARAM) == null ? null : Pattern.compile(params.get(EXCLUDED_TOPICS_PARAM));
        boolean useReadyDefaultGoals = Boolean.parseBoolean(params.get(USE_READY_DEFAULT_GOALS_PARAM));
        boolean excludeRecentlyDemotedBrokers = Boolean.parseBoolean(params.get(EXCLUDE_RECENTLY_DEMOTED_BROKERS_PARAM));
        boolean excludeRecentlyRemovedBrokers = Boolean.parseBoolean(params.get(EXCLUDE_RECENTLY_REMOVED_BROKERS_PARAM));
        String destinationBrokerIds = params.get(DESTINATION_BROKER_IDS_PARAM);
        boolean rebalanceDisk = Boolean.parseBoolean(params.get(REBALANCE_DISK_MODE_PARAM));
        boolean json = Boolean.parseBoolean(params.get(JSON_PARAM));
        boolean verbose = Boolean.parseBoolean(params.get(VERBOSE_PARAM));
        boolean fastMode = params.get(FAST_MODE_PARAM) == null || Boolean.parseBoolean(params.get(FAST_MODE_PARAM));
        ProposalsParameters proposalsParameters = new ProposalsParameters();
        try {
            proposalsParameters.initParameters(ignoreProposalCache, dataFrom, goals, kafkaAssigner, rebalanceDisk, allowCapacityEstimation,
                    excludedTopics, useReadyDefaultGoals, excludeRecentlyDemotedBrokers, excludeRecentlyRemovedBrokers, destinationBrokerIds,
                    rebalanceDisk, json, verbose, fastMode);
            OperationFuture future = new OperationFuture("Get customized proposals");
            ProposalsRunnable proposalsRunnable = new ProposalsRunnable(KafkaCruiseControlVertxApp.getVerticle().getKafkaCruiseControl(), future, proposalsParameters);
            OptimizationResult optimizationResult = proposalsRunnable.getResult();
            String outputString = json ? optimizationResult.getJSONString(verbose)
                    : optimizationResult.getPlaintext(verbose, optimizationResult.getPlaintextPretext(proposalsParameters));
            context.response()
                    .setStatusCode(RESPONSE_OK)
                    .end(outputString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
