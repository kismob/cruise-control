/*
 * Copyright 2021 LinkedIn Corp. Licensed under the BSD 2-Clause License (the "License"). See License in the project root for license information.
 */

package com.linkedin.kafka.cruisecontrol.vertx;

import com.codahale.metrics.Timer;
import com.linkedin.cruisecontrol.servlet.EndPoint;
import com.linkedin.kafka.cruisecontrol.async.AsyncKafkaCruiseControl;
import com.linkedin.kafka.cruisecontrol.servlet.UserTaskManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.vertx.ext.web.RoutingContext;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface SwaggerApi {

    @Operation(summary = "Gives partition healthiness on the cluster.", method = "GET", operationId = "kafka_cluster_state",
            tags = {
                    "GET"
            },
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "topic",
                            description = "Regular expression to filter partition state to report based on partition's topic.", schema = @Schema(type = "string")),
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
                                            "{\"KafkaPartitionState\":{\"offline\":[],\"urp\":[],\"with-offline-replicas\":[],\"under-min-isr\":[]},\"KafkaBrokerState\":{\"IsController\":{\"0\":true,\"1\":false,\"2\":false},\"OfflineLogDirsByBrokerId\":{\"0\":[],\"1\":[],\"2\":[]},\"ReplicaCountByBrokerId\":{\"0\":46,\"1\":44,\"2\":39},\"OutOfSyncCountByBrokerId\":{},\"Summary\":{\"StdLeadersPerBroker\":1.8856180831641267,\"Leaders\":65,\"MaxLeadersPerBroker\":23,\"Topics\":3,\"MaxReplicasPerBroker\":46,\"StdReplicasPerBroker\":2.943920288775949,\"Brokers\":3,\"AvgReplicationFactor\":1.9846153846153847,\"AvgLeadersPerBroker\":21.666666666666668,\"Replicas\":129,\"AvgReplicasPerBroker\":43.0},\"OnlineLogDirsByBrokerId\":{\"0\":[\"/tmp/kafka-logs-0\"],\"1\":[\"/tmp/kafka-logs-1\"],\"2\":[\"/tmp/kafka-logs-2\"]},\"LeaderCountByBrokerId\":{\"0\":23,\"1\":23,\"2\":19},\"OfflineReplicaCountByBrokerId\":{}},\"version\":1}"
                                            )
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Not found."),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error.")
            }
    )
    void kafkaClusterState(RoutingContext context);

    @Operation(summary = "Returns Cruise Control State", method = "GET", operationId = "state",
            tags = {
                    "GET"
            },
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "substates",
                            description = "Substates for which to retrieve state from cruise-control, available substates are analyzer, monitor, executor and anomaly_detector.", array = @ArraySchema(schema = @Schema(type = "string"))),
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
                                            "{\"AnalyzerState\":{\"isProposalReady\":false,\"readyGoals\":[]},\"MonitorState\":{\"trainingPct\":0.0,\"trained\":false,\"numFlawedPartitions\":0,\"state\":\"RUNNING\",\"numTotalPartitions\":0,\"numMonitoredWindows\":0,\"monitoringCoveragePct\":0.0,\"reasonOfLatestPauseOrResume\":\"N/A\",\"numValidPartitions\":0},\"ExecutorState\":{\"state\":\"NO_TASK_IN_PROGRESS\"},\"AnomalyDetectorState\":{\"recentBrokerFailures\":[],\"recentGoalViolations\":[],\"selfHealingDisabled\":[\"DISK_FAILURE\",\"BROKER_FAILURE\",\"GOAL_VIOLATION\",\"METRIC_ANOMALY\",\"TOPIC_ANOMALY\",\"MAINTENANCE_EVENT\"],\"balancednessScore\":100.0,\"selfHealingEnabled\":[],\"recentDiskFailures\":[],\"metrics\":{\"meanTimeToStartFixMs\":0.0,\"numSelfHealingStarted\":0,\"ongoingAnomalyDurationMs\":0,\"numSelfHealingFailedToStart\":0,\"meanTimeBetweenAnomaliesMs\":{\"DISK_FAILURE\":0.0,\"TOPIC_ANOMALY\":0.0,\"METRIC_ANOMALY\":0.0,\"GOAL_VIOLATION\":0.0,\"BROKER_FAILURE\":0.0,\"MAINTENANCE_EVENT\":0.0}},\"recentMetricAnomalies\":[],\"recentTopicAnomalies\":[],\"selfHealingEnabledRatio\":{\"DISK_FAILURE\":0.0,\"BROKER_FAILURE\":0.0,\"METRIC_ANOMALY\":0.0,\"GOAL_VIOLATION\":0.0,\"TOPIC_ANOMALY\":0.0,\"MAINTENANCE_EVENT\":0.0},\"recentMaintenanceEvents\":[]},\"version\":1}")
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Not found."),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error.")
            }
    )
    void cruiseControlState(RoutingContext context);

    @Operation(summary = "Once Cruise Control load Monitor shows it is in the RUNNING state, Users can use the following HTTP GET to get the cluster load.", method = "GET", operationId = "load",
            tags = {
                    "GET"
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
                                            "{\"brokers\":[{\"FollowerNwInRate\":0.0030495859682559967,\"BrokerState\":\"ALIVE\",\"Broker\":0,\"NwOutRate\":0.05964330886490643,\"NumCore\":1,\"Host\":\"172.30.67.28\",\"CpuPct\":0.09803944081068039,\"Replicas\":39,\"NetworkInCapacity\":50000.0,\"Rack\":\"172.30.67.28\",\"Leaders\":18,\"DiskCapacityMB\":500000.0,\"DiskMB\":1.7910490036010742,\"PnwOutRate\":0.06269291741773486,\"NetworkOutCapacity\":50000.0,\"LeaderNwInRate\":0.05129331350326538,\"DiskPct\":3.5820980072021486E-4}]")
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Not found."),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error.")
            }
    )
    void load(RoutingContext context);

    @Operation(summary = "Returns a full list of all the active/completed(and not recycled) tasks inside Cruise Control.", method = "GET", operationId = "user_tasks",
            tags = {
                    "GET"
            },
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "user_task_ids",
                            description = "Comma separated UUIDs to filter the task results Cruise Control report.", array = @ArraySchema(schema = @Schema(type = "string"))),
                    @Parameter(in = ParameterIn.QUERY, name = "client_ids",
                            description = "Comma separated IP addresses to filter the task results Cruise Control report.", array = @ArraySchema(schema = @Schema(type = "string"))),
                    @Parameter(in = ParameterIn.QUERY, name = "entries",
                            description = "Number of partition load entries to report in response.", schema = @Schema(type = "integer")),
                    @Parameter(in = ParameterIn.QUERY, name = "endpoints",
                            description = "Comma separated endpoints to filter the task results Cruise Control report.", array = @ArraySchema(schema = @Schema(type = "string"))),
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
                                            "{\"userTasks\":[{\"Status\":\"Completed\",\"ClientIdentity\":\"127.0.0.1\",\"RequestURL\":\"GET /state?json\\u003dtrue?json\\u003dtrue\",\"UserTaskId\":\"4af5cb3e-127f-4499-8e40-c7d14aa959d2\",\"StartMs\":\"1632211263537\"}]")
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Not found."),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error.")
            }
    )
    void userTasks(RoutingContext context);

    @Operation(summary = "Gives the partition load sorted by the utilization of a given resource.", method = "GET", operationId = "partition_load",
            tags = {
                    "GET"
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
                            description = "Regular expression to filter partition load to report based on partition's topic.", schema = @Schema(type = "string")),
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
                                            "{\"records\":[{\"networkOutbound\":0.05612216144800186,\"disk\":1.772721290588379,\"partition\":0,\"followers\":[],\"leader\":0,\"msg_in\":3.897528886795044,\"networkInbound\":0.04776539281010628,\"cpu\":0.07869745790958405,\"topic\":\"__CruiseControlMetrics\"}]")
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Not found."),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error.")
            }
    )
    void partitionLoad(RoutingContext context);

    @Operation(summary = "Returns the optimization proposals generated based on the workload model of the given timestamp. The workload summary before and after the optimization will also be returned.", method = "GET", operationId = "proposals",
            tags = {
                    "GET"
            },
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "ignore_proposal_cache",
                            description = "Whether to ignore the cached proposal or not.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "data_from",
                            description = "Whether to calculate proposal from available valid partitions or valid windows.", schema = @Schema(type = "string")),
                    @Parameter(in = ParameterIn.QUERY, name = "goals",
                            description = "List of goals used to generate proposal.", array = @ArraySchema(schema = @Schema(type = "string"))),
                    @Parameter(in = ParameterIn.QUERY, name = "kafka_assigner",
                            description = "Whether to use Kafka assigner mode to generate proposals.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "allow_capacity_estimation",
                            description = "Whether to allow broker capacity to be estimated.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "excluded_topics",
                            description = "Regular expression to specify topics excluded from replica and leadership movement.", schema = @Schema(type = "string")),
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
                                            "{\"summary\":{\"numIntraBrokerReplicaMovements\":0,\"numReplicaMovements\":22,\"onDemandBalancednessScoreAfter\":93.21842452909542,\"intraBrokerDataToMoveMB\":0,\"monitoredPartitionsPercentage\":100.0,\"provisionRecommendation\":\"\",\"excludedBrokersForReplicaMove\":[],\"excludedBrokersForLeadership\":[],\"provisionStatus\":\"RIGHT_SIZED\",\"onDemandBalancednessScoreBefore\":90.52689689611348,\"recentWindows\":5,\"dataToMoveMB\":0,\"excludedTopics\":[],\"numLeaderMovements\":2},\"goalSummary\":[{\"goal\":\"RackAwareDistributionGoal\",\"status\":\"NO-ACTION\",\"optimizationTimeMs\":0,\"clusterModelStats\":{\"metadata\":{\"topics\":3,\"brokers\":3,\"replicas\":129},\"statistics\":{\"AVG\":{\"disk\":0.6459576288859049,\"replicas\":43.0,\"leaderReplicas\":21.666666666666668,\"cpu\":0.09581887722015381,\"networkOutbound\":0.022673089988529682,\"networkInbound\":0.023853202660878498,\"topicReplicas\":14.333333333333334,\"potentialNwOut\":0.026638793702393432},\"STD\":{\"disk\":0.8268403579423366,\"replicas\":2.82842712474619,\"leaderReplicas\":2.6246692913372702,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.0,\"topicReplicas\":4.399775527382962,\"potentialNwOut\":0.022299485673227468},\"MIN\":{\"disk\":0.057915687561035156,\"replicas\":39,\"leaderReplicas\":18,\"cpu\":0.2874566316604614,\"networkOutbound\":0.06801926996558905,\"networkInbound\":0.0715596079826355,\"topicReplicas\":0,\"potentialNwOut\":0.008440279922607374},\"MAX\":{\"disk\":1.8152799606323242,\"replicas\":45,\"leaderReplicas\":24,\"cpu\":0.2874566316604614,\"networkOutbound\":0.06801926996558905,\"networkInbound\":0.0715596079826355,\"topicReplicas\":28,\"potentialNwOut\":0.06302393274381757}}}}]")
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Not found."),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error.")
            }
    )
    void proposals(RoutingContext context);

    @Operation(summary = "The following POST request will let Kafka Cruise Control rebalance a Kafka cluster.", method = "POST", operationId = "rebalance",
            tags = {
                    "POST"
            },
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "dryrun",
                            description = "Whether dry-run the request or not.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "data_from",
                            description = "Whether to calculate proposal from available valid partitions or valid windows.", schema = @Schema(type = "string")),
                    @Parameter(in = ParameterIn.QUERY, name = "goals",
                            description = "List of goals used to generate proposal.", array = @ArraySchema(schema = @Schema(type = "string"))),
                    @Parameter(in = ParameterIn.QUERY, name = "kafka_assigner",
                            description = "Whether to use Kafka assigner mode to generate proposals.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "allow_capacity_estimation",
                            description = "Whether to allow broker capacity to be estimated.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "concurrent_partition_movements_per_broker",
                            description = "Upper bound of ongoing replica movements going into/out of each broker.", schema = @Schema(type = "integer")),
                    @Parameter(in = ParameterIn.QUERY, name = "concurrent_intra_broker_partition_movements",
                            description = "Upper bound of ongoing replica movements between disks within each broker.", schema = @Schema(type = "integer")),
                    @Parameter(in = ParameterIn.QUERY, name = "concurrent_leader_movements",
                            description = "Upper bound of ongoing leadership movements.", schema = @Schema(type = "integer")),
                    @Parameter(in = ParameterIn.QUERY, name = "skip_hard_goal_check",
                            description = "Whether allow hard goals be skipped in proposal generation.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "excluded_topics",
                            description = "Regular expression to specify topics excluded from replica and leadership movement.", schema = @Schema(type = "string")),
                    @Parameter(in = ParameterIn.QUERY, name = "use_ready_default_goals",
                            description = "Whether only use ready goals to generate proposal.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "exclude_recently_demoted_brokers",
                            description = "Whether to allow leader replicas to be moved to recently demoted brokers.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "exclude_recently_removed_brokers",
                            description = "Whether allow replicas to be moved to recently removed broker", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "replica_movement_strategies",
                            description = "Replica movement strategy to use.", schema = @Schema(type = "string")),
                    @Parameter(in = ParameterIn.QUERY, name = "ignore_proposal_cache",
                            description = "Whether to ignore the cached proposal or not.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "replication_throttle",
                            description = "Upper bound on the bandwidth used to move replicas (in bytes per second).", schema = @Schema(type = "long")),
                    @Parameter(in = ParameterIn.QUERY, name = "destination_broker_ids",
                            description = "Specify brokers to move replicas to.", schema = @Schema(type = "long")),
                    @Parameter(in = ParameterIn.QUERY, name = "rebalance_disk",
                            description = "Whether to balance load between disks within brokers (requires JBOD Kafka deployment).", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "json",
                            description = "Return in JSON format or not.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "verbose",
                            description = "Return detailed state information.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "reason",
                            description = "Reason for the request.", schema = @Schema(type = "string")),
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
                                            "{\"summary\":{\"numIntraBrokerReplicaMovements\":0,\"numReplicaMovements\":22,\"onDemandBalancednessScoreAfter\":93.21842452909542,\"intraBrokerDataToMoveMB\":0,\"monitoredPartitionsPercentage\":100.0,\"provisionRecommendation\":\"\",\"excludedBrokersForReplicaMove\":[],\"excludedBrokersForLeadership\":[],\"provisionStatus\":\"RIGHT_SIZED\",\"onDemandBalancednessScoreBefore\":90.52689689611348,\"recentWindows\":5,\"dataToMoveMB\":0,\"excludedTopics\":[],\"numLeaderMovements\":2},\"goalSummary\":[{\"goal\":\"RackAwareDistributionGoal\",\"status\":\"NO-ACTION\",\"optimizationTimeMs\":0,\"clusterModelStats\":{\"metadata\":{\"topics\":3,\"brokers\":3,\"replicas\":129},\"statistics\":{\"AVG\":{\"disk\":0.6459576288859049,\"replicas\":43.0,\"leaderReplicas\":21.666666666666668,\"cpu\":0.09581887722015381,\"networkOutbound\":0.022673089988529682,\"networkInbound\":0.023853202660878498,\"topicReplicas\":14.333333333333334,\"potentialNwOut\":0.026638793702393432},\"STD\":{\"disk\":0.8268403579423366,\"replicas\":2.82842712474619,\"leaderReplicas\":2.6246692913372702,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.0,\"topicReplicas\":4.399775527382962,\"potentialNwOut\":0.022299485673227468},\"MIN\":{\"disk\":0.057915687561035156,\"replicas\":39,\"leaderReplicas\":18,\"cpu\":0.2874566316604614,\"networkOutbound\":0.06801926996558905,\"networkInbound\":0.0715596079826355,\"topicReplicas\":0,\"potentialNwOut\":0.008440279922607374},\"MAX\":{\"disk\":1.8152799606323242,\"replicas\":45,\"leaderReplicas\":24,\"cpu\":0.2874566316604614,\"networkOutbound\":0.06801926996558905,\"networkInbound\":0.0715596079826355,\"topicReplicas\":28,\"potentialNwOut\":0.06302393274381757}}}}]")
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Not found."),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error.")
            }
    )
    void rebalance(RoutingContext context);

    @Operation(summary = "The following POST request adds the given brokers to the Kafka cluster.", method = "POST", operationId = "add_broker",
            tags = {
                    "POST"
            },
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "brokerid",
                            description = "List of ids of new broker added to the cluster.", array = @ArraySchema(schema = @Schema(type = "integer")), required = true),
                    @Parameter(in = ParameterIn.QUERY, name = "dryrun",
                            description = "Whether dry-run the request or not.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "data_from",
                            description = "Whether to calculate proposal from available valid partitions or valid windows.", schema = @Schema(type = "string")),
                    @Parameter(in = ParameterIn.QUERY, name = "goals",
                            description = "List of goals used to generate proposal.", array = @ArraySchema(schema = @Schema(type = "string"))),
                    @Parameter(in = ParameterIn.QUERY, name = "kafka_assigner",
                            description = "Whether to use Kafka assigner mode to generate proposals.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "allow_capacity_estimation",
                            description = "Whether to allow broker capacity to be estimated.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "concurrent_partition_movements_per_broker",
                            description = "Upper bound of ongoing replica movements going into/out of each broker.", schema = @Schema(type = "integer")),
                    @Parameter(in = ParameterIn.QUERY, name = "concurrent_leader_movements",
                            description = "Upper bound of ongoing leadership movements.", schema = @Schema(type = "integer")),
                    @Parameter(in = ParameterIn.QUERY, name = "skip_hard_goal_check",
                            description = "Whether allow hard goals be skipped in proposal generation.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "excluded_topics",
                            description = "Regular expression to specify topics excluded from replica and leadership movement.", schema = @Schema(type = "string")),
                    @Parameter(in = ParameterIn.QUERY, name = "use_ready_default_goals",
                            description = "Whether only use ready goals to generate proposal.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "exclude_recently_demoted_brokers",
                            description = "Whether to allow leader replicas to be moved to recently demoted brokers.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "exclude_recently_removed_brokers",
                            description = "Whether allow replicas to be moved to recently removed broker", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "replica_movement_strategies",
                            description = "Replica movement strategy to use.", schema = @Schema(type = "string")),
                    @Parameter(in = ParameterIn.QUERY, name = "replication_throttle",
                            description = "Upper bound on the bandwidth used to move replicas (in bytes per second).", schema = @Schema(type = "long")),
                    @Parameter(in = ParameterIn.QUERY, name = "throttle_added_broker",
                            description = "Whether throttle replica movement to new broker or not.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "json",
                            description = "Return in JSON format or not.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "verbose",
                            description = "Return detailed state information.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "reason",
                            description = "Reason for the request.", schema = @Schema(type = "string")),
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
                                            "{\"summary\":{\"numIntraBrokerReplicaMovements\":0,\"numReplicaMovements\":22,\"onDemandBalancednessScoreAfter\":93.21842452909542,\"intraBrokerDataToMoveMB\":0,\"monitoredPartitionsPercentage\":100.0,\"provisionRecommendation\":\"\",\"excludedBrokersForReplicaMove\":[],\"excludedBrokersForLeadership\":[],\"provisionStatus\":\"RIGHT_SIZED\",\"onDemandBalancednessScoreBefore\":90.52689689611348,\"recentWindows\":5,\"dataToMoveMB\":0,\"excludedTopics\":[],\"numLeaderMovements\":2},\"goalSummary\":[{\"goal\":\"RackAwareDistributionGoal\",\"status\":\"NO-ACTION\",\"optimizationTimeMs\":0,\"clusterModelStats\":{\"metadata\":{\"topics\":3,\"brokers\":3,\"replicas\":129},\"statistics\":{\"AVG\":{\"disk\":0.6459576288859049,\"replicas\":43.0,\"leaderReplicas\":21.666666666666668,\"cpu\":0.09581887722015381,\"networkOutbound\":0.022673089988529682,\"networkInbound\":0.023853202660878498,\"topicReplicas\":14.333333333333334,\"potentialNwOut\":0.026638793702393432},\"STD\":{\"disk\":0.8268403579423366,\"replicas\":2.82842712474619,\"leaderReplicas\":2.6246692913372702,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.0,\"topicReplicas\":4.399775527382962,\"potentialNwOut\":0.022299485673227468},\"MIN\":{\"disk\":0.057915687561035156,\"replicas\":39,\"leaderReplicas\":18,\"cpu\":0.2874566316604614,\"networkOutbound\":0.06801926996558905,\"networkInbound\":0.0715596079826355,\"topicReplicas\":0,\"potentialNwOut\":0.008440279922607374},\"MAX\":{\"disk\":1.8152799606323242,\"replicas\":45,\"leaderReplicas\":24,\"cpu\":0.2874566316604614,\"networkOutbound\":0.06801926996558905,\"networkInbound\":0.0715596079826355,\"topicReplicas\":28,\"potentialNwOut\":0.06302393274381757}}}}]")
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Not found."),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error.")
            }
    )
    void addBroker(RoutingContext context);

    @Operation(summary = "The following POST request removes a list of brokers from the Kafka cluster.", method = "POST", operationId = "remove_broker",
            tags = {
                    "POST"
            },
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "brokerid",
                            description = "List of ids of broker to be removed from the cluster.", array = @ArraySchema(schema = @Schema(type = "integer")), required = true),
                    @Parameter(in = ParameterIn.QUERY, name = "dryrun",
                            description = "Whether dry-run the request or not.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "data_from",
                            description = "Whether to calculate proposal from available valid partitions or valid windows.", schema = @Schema(type = "string")),
                    @Parameter(in = ParameterIn.QUERY, name = "goals",
                            description = "List of goals used to generate proposal.", array = @ArraySchema(schema = @Schema(type = "string"))),
                    @Parameter(in = ParameterIn.QUERY, name = "kafka_assigner",
                            description = "Whether to use Kafka assigner mode to generate proposals.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "allow_capacity_estimation",
                            description = "Whether to allow broker capacity to be estimated.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "concurrent_partition_movements_per_broker",
                            description = "Upper bound of ongoing replica movements going into/out of each broker.", schema = @Schema(type = "integer")),
                    @Parameter(in = ParameterIn.QUERY, name = "concurrent_leader_movements",
                            description = "Upper bound of ongoing leadership movements.", schema = @Schema(type = "integer")),
                    @Parameter(in = ParameterIn.QUERY, name = "skip_hard_goal_check",
                            description = "Whether allow hard goals be skipped in proposal generation.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "excluded_topics",
                            description = "Regular expression to specify topics excluded from replica and leadership movement.", schema = @Schema(type = "string")),
                    @Parameter(in = ParameterIn.QUERY, name = "use_ready_default_goals",
                            description = "Whether only use ready goals to generate proposal.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "exclude_recently_demoted_brokers",
                            description = "Whether to allow leader replicas to be moved to recently demoted brokers.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "exclude_recently_removed_brokers",
                            description = "Whether allow replicas to be moved to recently removed broker", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "replica_movement_strategies",
                            description = "Replica movement strategy to use.", schema = @Schema(type = "string")),
                    @Parameter(in = ParameterIn.QUERY, name = "replication_throttle",
                            description = "Upper bound on the bandwidth used to move replicas (in bytes per second).", schema = @Schema(type = "long")),
                    @Parameter(in = ParameterIn.QUERY, name = "throttle_removed_broker",
                            description = "Whether throttle replica movement out of the removed broker or not.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "destination_broker_ids",
                            description = "Specify brokers to move replicas to.", array = @ArraySchema(schema = @Schema(type = "integer"))),
                    @Parameter(in = ParameterIn.QUERY, name = "json",
                            description = "Return in JSON format or not.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "verbose",
                            description = "Return detailed state information.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "reason",
                            description = "Reason for the request.", schema = @Schema(type = "string")),
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
                                            "{\"summary\":{\"numIntraBrokerReplicaMovements\":0,\"numReplicaMovements\":22,\"onDemandBalancednessScoreAfter\":93.21842452909542,\"intraBrokerDataToMoveMB\":0,\"monitoredPartitionsPercentage\":100.0,\"provisionRecommendation\":\"\",\"excludedBrokersForReplicaMove\":[],\"excludedBrokersForLeadership\":[],\"provisionStatus\":\"RIGHT_SIZED\",\"onDemandBalancednessScoreBefore\":90.52689689611348,\"recentWindows\":5,\"dataToMoveMB\":0,\"excludedTopics\":[],\"numLeaderMovements\":2},\"goalSummary\":[{\"goal\":\"RackAwareDistributionGoal\",\"status\":\"NO-ACTION\",\"optimizationTimeMs\":0,\"clusterModelStats\":{\"metadata\":{\"topics\":3,\"brokers\":3,\"replicas\":129},\"statistics\":{\"AVG\":{\"disk\":0.6459576288859049,\"replicas\":43.0,\"leaderReplicas\":21.666666666666668,\"cpu\":0.09581887722015381,\"networkOutbound\":0.022673089988529682,\"networkInbound\":0.023853202660878498,\"topicReplicas\":14.333333333333334,\"potentialNwOut\":0.026638793702393432},\"STD\":{\"disk\":0.8268403579423366,\"replicas\":2.82842712474619,\"leaderReplicas\":2.6246692913372702,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.0,\"topicReplicas\":4.399775527382962,\"potentialNwOut\":0.022299485673227468},\"MIN\":{\"disk\":0.057915687561035156,\"replicas\":39,\"leaderReplicas\":18,\"cpu\":0.2874566316604614,\"networkOutbound\":0.06801926996558905,\"networkInbound\":0.0715596079826355,\"topicReplicas\":0,\"potentialNwOut\":0.008440279922607374},\"MAX\":{\"disk\":1.8152799606323242,\"replicas\":45,\"leaderReplicas\":24,\"cpu\":0.2874566316604614,\"networkOutbound\":0.06801926996558905,\"networkInbound\":0.0715596079826355,\"topicReplicas\":28,\"potentialNwOut\":0.06302393274381757}}}}]")
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Not found."),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error.")
            }
    )
    void removeBroker(RoutingContext context);

    @Operation(summary = "The following POST request moves all the offline replicas from dead disks/brokers.", method = "POST", operationId = "fix_offline_replicas",
            tags = {
                    "POST"
            },
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "dryrun",
                            description = "Whether dry-run the request or not.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "data_from",
                            description = "Whether to calculate proposal from available valid partitions or valid windows.", schema = @Schema(type = "string")),
                    @Parameter(in = ParameterIn.QUERY, name = "goals",
                            description = "List of goals used to generate proposal.", array = @ArraySchema(schema = @Schema(type = "string"))),
                    @Parameter(in = ParameterIn.QUERY, name = "kafka_assigner",
                            description = "Whether to use Kafka assigner mode to generate proposals.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "allow_capacity_estimation",
                            description = "Whether to allow broker capacity to be estimated.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "concurrent_partition_movements_per_broker",
                            description = "Upper bound of ongoing replica movements going into/out of each broker.", schema = @Schema(type = "integer")),
                    @Parameter(in = ParameterIn.QUERY, name = "concurrent_leader_movements",
                            description = "Upper bound of ongoing leadership movements.", schema = @Schema(type = "integer")),
                    @Parameter(in = ParameterIn.QUERY, name = "skip_hard_goal_check",
                            description = "Whether allow hard goals be skipped in proposal generation.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "excluded_topics",
                            description = "Regular expression to specify topics excluded from replica and leadership movement.", schema = @Schema(type = "string")),
                    @Parameter(in = ParameterIn.QUERY, name = "use_ready_default_goals",
                            description = "Whether only use ready goals to generate proposal.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "exclude_recently_demoted_brokers",
                            description = "Whether to allow leader replicas to be moved to recently demoted brokers.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "exclude_recently_removed_brokers",
                            description = "Whether allow replicas to be moved to recently removed broker", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "replica_movement_strategies",
                            description = "Replica movement strategy to use.", schema = @Schema(type = "string")),
                    @Parameter(in = ParameterIn.QUERY, name = "replication_throttle",
                            description = "Upper bound on the bandwidth used to move replicas (in bytes per second).", schema = @Schema(type = "long")),
                    @Parameter(in = ParameterIn.QUERY, name = "json",
                            description = "Return in JSON format or not.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "verbose",
                            description = "Return detailed state information.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "reason",
                            description = "Reason for the request.", schema = @Schema(type = "string")),
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
                                            "{\"summary\":{\"numIntraBrokerReplicaMovements\":0,\"numReplicaMovements\":22,\"onDemandBalancednessScoreAfter\":93.21842452909542,\"intraBrokerDataToMoveMB\":0,\"monitoredPartitionsPercentage\":100.0,\"provisionRecommendation\":\"\",\"excludedBrokersForReplicaMove\":[],\"excludedBrokersForLeadership\":[],\"provisionStatus\":\"RIGHT_SIZED\",\"onDemandBalancednessScoreBefore\":90.52689689611348,\"recentWindows\":5,\"dataToMoveMB\":0,\"excludedTopics\":[],\"numLeaderMovements\":2},\"goalSummary\":[{\"goal\":\"RackAwareDistributionGoal\",\"status\":\"NO-ACTION\",\"optimizationTimeMs\":0,\"clusterModelStats\":{\"metadata\":{\"topics\":3,\"brokers\":3,\"replicas\":129},\"statistics\":{\"AVG\":{\"disk\":0.6459576288859049,\"replicas\":43.0,\"leaderReplicas\":21.666666666666668,\"cpu\":0.09581887722015381,\"networkOutbound\":0.022673089988529682,\"networkInbound\":0.023853202660878498,\"topicReplicas\":14.333333333333334,\"potentialNwOut\":0.026638793702393432},\"STD\":{\"disk\":0.8268403579423366,\"replicas\":2.82842712474619,\"leaderReplicas\":2.6246692913372702,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.0,\"topicReplicas\":4.399775527382962,\"potentialNwOut\":0.022299485673227468},\"MIN\":{\"disk\":0.057915687561035156,\"replicas\":39,\"leaderReplicas\":18,\"cpu\":0.2874566316604614,\"networkOutbound\":0.06801926996558905,\"networkInbound\":0.0715596079826355,\"topicReplicas\":0,\"potentialNwOut\":0.008440279922607374},\"MAX\":{\"disk\":1.8152799606323242,\"replicas\":45,\"leaderReplicas\":24,\"cpu\":0.2874566316604614,\"networkOutbound\":0.06801926996558905,\"networkInbound\":0.0715596079826355,\"topicReplicas\":28,\"potentialNwOut\":0.06302393274381757}}}}]")
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Not found."),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error.")
            }
    )
    void fixOfflineReplicas(RoutingContext context);

    @Operation(summary = "The following POST request moves all the leader replicas away from a list of brokers.", method = "POST", operationId = "demote_broker",
            tags = {
                    "POST"
            },
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "brokerid",
                            description = "List of ids of broker to be demoted in the cluster.", array = @ArraySchema(schema = @Schema(type = "integer"))),
                    @Parameter(in = ParameterIn.QUERY, name = "brokerid_and_logdirs",
                            description = "List of broker id and logdir pair to be demoted in the cluster.", array = @ArraySchema(schema = @Schema(type = "string"))),
                    @Parameter(in = ParameterIn.QUERY, name = "dryrun",
                            description = "Whether dry-run the request or not.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "allow_capacity_estimation",
                            description = "Whether to allow broker capacity to be estimated.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "concurrent_leader_movements",
                            description = "Upper bound of ongoing leadership movements.", schema = @Schema(type = "integer")),
                    @Parameter(in = ParameterIn.QUERY, name = "skip_urp_demotion",
                            description = "Whether skip demoting leader replicas for under replicated partitions.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "exclude_follower_demotion",
                            description = "Whether skip demoting follower replicas on the broker to be demoted.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "exclude_recently_demoted_brokers",
                            description = "Whether to allow leader replicas to be moved to recently demoted brokers.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "replica_movement_strategies",
                            description = "Replica movement strategy to use.", schema = @Schema(type = "string")),
                    @Parameter(in = ParameterIn.QUERY, name = "replication_throttle",
                            description = "Upper bound on the bandwidth used to move replicas (in bytes per second).", schema = @Schema(type = "long")),
                    @Parameter(in = ParameterIn.QUERY, name = "json",
                            description = "Return in JSON format or not.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "verbose",
                            description = "Return detailed state information.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "reason",
                            description = "Reason for the request.", schema = @Schema(type = "string")),
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
                                            "{\"summary\":{\"numIntraBrokerReplicaMovements\":0,\"numReplicaMovements\":22,\"onDemandBalancednessScoreAfter\":93.21842452909542,\"intraBrokerDataToMoveMB\":0,\"monitoredPartitionsPercentage\":100.0,\"provisionRecommendation\":\"\",\"excludedBrokersForReplicaMove\":[],\"excludedBrokersForLeadership\":[],\"provisionStatus\":\"RIGHT_SIZED\",\"onDemandBalancednessScoreBefore\":90.52689689611348,\"recentWindows\":5,\"dataToMoveMB\":0,\"excludedTopics\":[],\"numLeaderMovements\":2},\"goalSummary\":[{\"goal\":\"RackAwareDistributionGoal\",\"status\":\"NO-ACTION\",\"optimizationTimeMs\":0,\"clusterModelStats\":{\"metadata\":{\"topics\":3,\"brokers\":3,\"replicas\":129},\"statistics\":{\"AVG\":{\"disk\":0.6459576288859049,\"replicas\":43.0,\"leaderReplicas\":21.666666666666668,\"cpu\":0.09581887722015381,\"networkOutbound\":0.022673089988529682,\"networkInbound\":0.023853202660878498,\"topicReplicas\":14.333333333333334,\"potentialNwOut\":0.026638793702393432},\"STD\":{\"disk\":0.8268403579423366,\"replicas\":2.82842712474619,\"leaderReplicas\":2.6246692913372702,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.0,\"topicReplicas\":4.399775527382962,\"potentialNwOut\":0.022299485673227468},\"MIN\":{\"disk\":0.057915687561035156,\"replicas\":39,\"leaderReplicas\":18,\"cpu\":0.2874566316604614,\"networkOutbound\":0.06801926996558905,\"networkInbound\":0.0715596079826355,\"topicReplicas\":0,\"potentialNwOut\":0.008440279922607374},\"MAX\":{\"disk\":1.8152799606323242,\"replicas\":45,\"leaderReplicas\":24,\"cpu\":0.2874566316604614,\"networkOutbound\":0.06801926996558905,\"networkInbound\":0.0715596079826355,\"topicReplicas\":28,\"potentialNwOut\":0.06302393274381757}}}}]")
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Not found."),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error.")
            }
    )
    void demoteBroker(RoutingContext context);

    @Operation(summary = "The following POST request will let Kafka Cruise Control stop an ongoing rebalance, add_broker, remove_broker, fix_offline_replica, topic_configuration or demote_broker operation.", method = "POST", operationId = "stop_proposal_execution",
            tags = {
                    "POST"
            },
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "force_stop",
                            description = "(not supported in Kafka 2.4 or above) stop an ongoing execution forcefully by deleting Kafka internal zNodes", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "stop_external_agent",
                            description = "(required Kafka 2.4 or above) stop an ongoing execution even if it is started by an external agent.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "review_id",
                            description = "Review id for 2-step verification.", schema = @Schema(type = "integer")),
                    @Parameter(in = ParameterIn.QUERY, name = "json",
                            description = "Return in JSON format or not.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "get_response_schema",
                            description = "Return JSON schema in response header or not.", schema = @Schema(type = "boolean")),
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
                                            "Proposal execution stopped.")
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Not found."),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error.")
            }
    )
    void stopProposalExecution(RoutingContext context);

    @Operation(summary = "The following POST request will let Kafka Cruise Control pause an ongoing metrics sampling process.", method = "POST", operationId = "pause_sampling",
            tags = {
                    "POST"
            },
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "reason",
                            description = "Reason to pause sampling.", schema = @Schema(type = "integer")),
                    @Parameter(in = ParameterIn.QUERY, name = "json",
                            description = "Return in JSON format or not.", schema = @Schema(type = "boolean")),
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
                                            "Metric sampling paused.")
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Not found."),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error.")
            }
    )
    void pauseSampling(RoutingContext context);

    @Operation(summary = "The following POST request will let Kafka Cruise Control resume a paused metrics sampling process.", method = "POST", operationId = "resume_sampling",
            tags = {
                    "POST"
            },
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "reason",
                            description = "Reason to resume sampling.", schema = @Schema(type = "integer")),
                    @Parameter(in = ParameterIn.QUERY, name = "json",
                            description = "Return in JSON format or not.", schema = @Schema(type = "boolean")),
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
                                            "Metric sampling resumed.")
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Not found."),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error.")
            }
    )
    void resumeSampling(RoutingContext context);

    @Operation(summary = "The following POST request can change topic's replication factor.", method = "POST", operationId = "topic_configuration",
            tags = {
                    "POST"
            },
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "dryrun",
                            description = "Whether dry-run the request or not.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "topic",
                            description = "Regular expression to specify subject topics.", schema = @Schema(type = "string"), required = true),
                    @Parameter(in = ParameterIn.QUERY, name = "replication_factor",
                            description = "Target replication factor.", schema = @Schema(type = "integer"), required = true),
                    @Parameter(in = ParameterIn.QUERY, name = "data_from",
                            description = "Whether to calculate proposal from available valid partitions or valid windows.", schema = @Schema(type = "string")),
                    @Parameter(in = ParameterIn.QUERY, name = "goals",
                            description = "List of goals used to generate proposal.", array = @ArraySchema(schema = @Schema(type = "string"))),
                    @Parameter(in = ParameterIn.QUERY, name = "allow_capacity_estimation",
                            description = "Whether to allow broker capacity to be estimated.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "concurrent_partition_movements_per_broker",
                            description = "Upper bound of ongoing replica movements going into/out of each broker.", schema = @Schema(type = "integer")),
                    @Parameter(in = ParameterIn.QUERY, name = "concurrent_leader_movements",
                            description = "Upper bound of ongoing leadership movements.", schema = @Schema(type = "integer")),
                    @Parameter(in = ParameterIn.QUERY, name = "skip_hard_goal_check",
                            description = "Whether allow hard goals be skipped in proposal generation.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "use_ready_default_goals",
                            description = "Whether only use ready goals to generate proposal.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "exclude_recently_demoted_brokers",
                            description = "Whether to allow leader replicas to be moved to recently demoted brokers.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "exclude_recently_removed_brokers",
                            description = "Whether allow replicas to be moved to recently removed broker", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "replica_movement_strategies",
                            description = "Replica movement strategy to use.", schema = @Schema(type = "string")),
                    @Parameter(in = ParameterIn.QUERY, name = "replication_throttle",
                            description = "Upper bound on the bandwidth used to move replicas (in bytes per second).", schema = @Schema(type = "long")),
                    @Parameter(in = ParameterIn.QUERY, name = "json",
                            description = "Return in JSON format or not.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "verbose",
                            description = "Return detailed state information.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "reason",
                            description = "Reason for the request.", schema = @Schema(type = "string")),
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
                                            "{\"summary\":{\"numIntraBrokerReplicaMovements\":0,\"numReplicaMovements\":22,\"onDemandBalancednessScoreAfter\":93.21842452909542,\"intraBrokerDataToMoveMB\":0,\"monitoredPartitionsPercentage\":100.0,\"provisionRecommendation\":\"\",\"excludedBrokersForReplicaMove\":[],\"excludedBrokersForLeadership\":[],\"provisionStatus\":\"RIGHT_SIZED\",\"onDemandBalancednessScoreBefore\":90.52689689611348,\"recentWindows\":5,\"dataToMoveMB\":0,\"excludedTopics\":[],\"numLeaderMovements\":2},\"goalSummary\":[{\"goal\":\"RackAwareDistributionGoal\",\"status\":\"NO-ACTION\",\"optimizationTimeMs\":0,\"clusterModelStats\":{\"metadata\":{\"topics\":3,\"brokers\":3,\"replicas\":129},\"statistics\":{\"AVG\":{\"disk\":0.6459576288859049,\"replicas\":43.0,\"leaderReplicas\":21.666666666666668,\"cpu\":0.09581887722015381,\"networkOutbound\":0.022673089988529682,\"networkInbound\":0.023853202660878498,\"topicReplicas\":14.333333333333334,\"potentialNwOut\":0.026638793702393432},\"STD\":{\"disk\":0.8268403579423366,\"replicas\":2.82842712474619,\"leaderReplicas\":2.6246692913372702,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.0,\"topicReplicas\":4.399775527382962,\"potentialNwOut\":0.022299485673227468},\"MIN\":{\"disk\":0.057915687561035156,\"replicas\":39,\"leaderReplicas\":18,\"cpu\":0.2874566316604614,\"networkOutbound\":0.06801926996558905,\"networkInbound\":0.0715596079826355,\"topicReplicas\":0,\"potentialNwOut\":0.008440279922607374},\"MAX\":{\"disk\":1.8152799606323242,\"replicas\":45,\"leaderReplicas\":24,\"cpu\":0.2874566316604614,\"networkOutbound\":0.06801926996558905,\"networkInbound\":0.0715596079826355,\"topicReplicas\":28,\"potentialNwOut\":0.06302393274381757}}}}]")
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Not found."),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error.")
            }
    )
    void topicConfiguration(RoutingContext context);

    @Operation(summary = "Some Cruise Control configs can be changed dynamically via admin endpoint.", method = "POST", operationId = "admin",
            tags = {
                    "POST"
            },
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "disable_self_healing_for",
                            description = "List of anomaly types to disable self-healing.", array = @ArraySchema(schema = @Schema(type = "string"))),
                    @Parameter(in = ParameterIn.QUERY, name = "enable_self_healing_for",
                            description = "List of anomaly types to enable self-healing.", array = @ArraySchema(schema = @Schema(type = "string"))),
                    @Parameter(in = ParameterIn.QUERY, name = "concurrent_partition_movements_per_broker",
                            description = "Upper bound of ongoing replica movements into/out of a broker.", schema = @Schema(type = "integer")),
                    @Parameter(in = ParameterIn.QUERY, name = "concurrent_intra_broker_partition_movements",
                            description = "Upper bound of ongoing replica movements between disks within a broker.", schema = @Schema(type = "integer")),
                    @Parameter(in = ParameterIn.QUERY, name = "concurrent_leader_movements",
                            description = "Upper bound of ongoing leadership movements.", schema = @Schema(type = "integer")),
                    @Parameter(in = ParameterIn.QUERY, name = "drop_recently_removed_brokers",
                            description = "List of id of recently removed brokers to be dropped.", array = @ArraySchema(schema = @Schema(type = "integer"))),
                    @Parameter(in = ParameterIn.QUERY, name = "drop_recently_demoted_brokers",
                            description = "List of id of recently demoted brokers to be dropped.", array = @ArraySchema(schema = @Schema(type = "integer"))),
                    @Parameter(in = ParameterIn.QUERY, name = "doAs",
                            description = "Propagated user by the trusted proxy service.", schema = @Schema(type = "string")),
                    @Parameter(in = ParameterIn.QUERY, name = "json",
                            description = "Return in JSON format or not.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "review_id",
                            description = "Review id for 2-step verification.", schema = @Schema(type = "integer")),
                    @Parameter(in = ParameterIn.QUERY, name = "execution_progress_check_interval_ms",
                            description = "Execution progress check interval in milliseconds.", schema = @Schema(type = "long")),
                    @Parameter(in = ParameterIn.QUERY, name = "get_response_schema",
                            description = "Return JSON schema in response header or not.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "disable_concurrency_adjuster_for",
                            description = "Disable concurrency adjuster for given concurrency types.", array = @ArraySchema(schema = @Schema(type = "string"))),
                    @Parameter(in = ParameterIn.QUERY, name = "enable_concurrency_adjuster_for",
                            description = "Enable concurrency adjuster for given concurrency types.", array = @ArraySchema(schema = @Schema(type = "string"))),
                    @Parameter(in = ParameterIn.QUERY, name = "min_isr_based_concurrency_adjustment",
                            description = "Enable (true) or disable (false) MinISR-based concurrency adjustment.", schema = @Schema(type = "boolean"))
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    encoding = @Encoding(contentType = "application/json"),
                                    schema = @Schema(name = "jsonState", example =
                                            "{\"summary\":{\"numIntraBrokerReplicaMovements\":0,\"numReplicaMovements\":22,\"onDemandBalancednessScoreAfter\":93.21842452909542,\"intraBrokerDataToMoveMB\":0,\"monitoredPartitionsPercentage\":100.0,\"provisionRecommendation\":\"\",\"excludedBrokersForReplicaMove\":[],\"excludedBrokersForLeadership\":[],\"provisionStatus\":\"RIGHT_SIZED\",\"onDemandBalancednessScoreBefore\":90.52689689611348,\"recentWindows\":5,\"dataToMoveMB\":0,\"excludedTopics\":[],\"numLeaderMovements\":2},\"goalSummary\":[{\"goal\":\"RackAwareDistributionGoal\",\"status\":\"NO-ACTION\",\"optimizationTimeMs\":0,\"clusterModelStats\":{\"metadata\":{\"topics\":3,\"brokers\":3,\"replicas\":129},\"statistics\":{\"AVG\":{\"disk\":0.6459576288859049,\"replicas\":43.0,\"leaderReplicas\":21.666666666666668,\"cpu\":0.09581887722015381,\"networkOutbound\":0.022673089988529682,\"networkInbound\":0.023853202660878498,\"topicReplicas\":14.333333333333334,\"potentialNwOut\":0.026638793702393432},\"STD\":{\"disk\":0.8268403579423366,\"replicas\":2.82842712474619,\"leaderReplicas\":2.6246692913372702,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.0,\"topicReplicas\":4.399775527382962,\"potentialNwOut\":0.022299485673227468},\"MIN\":{\"disk\":0.057915687561035156,\"replicas\":39,\"leaderReplicas\":18,\"cpu\":0.2874566316604614,\"networkOutbound\":0.06801926996558905,\"networkInbound\":0.0715596079826355,\"topicReplicas\":0,\"potentialNwOut\":0.008440279922607374},\"MAX\":{\"disk\":1.8152799606323242,\"replicas\":45,\"leaderReplicas\":24,\"cpu\":0.2874566316604614,\"networkOutbound\":0.06801926996558905,\"networkInbound\":0.0715596079826355,\"topicReplicas\":28,\"potentialNwOut\":0.06302393274381757}}}}]")
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Not found."),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error.")
            }
    )
    void admin(RoutingContext context);

    @Operation(summary = "The following POST request can create a request to the provisioner to rightsize the broker or partition of a cluster.", method = "POST", operationId = "rightsize",
            tags = {
                    "POST"
            },
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "num_brokers_to_add",
                            description = "Difference in broker count to rightsize towards.", schema = @Schema(type = "integer")),
                    @Parameter(in = ParameterIn.QUERY, name = "partition_count",
                            description = "Target number of partitions to rightsize towards.", schema = @Schema(type = "integer")),
                    @Parameter(in = ParameterIn.QUERY, name = "topic",
                            description = "Regular expression to specify subject topics.", schema = @Schema(type = "string")),
                    @Parameter(in = ParameterIn.QUERY, name = "doAs",
                            description = "Propagated user by the trusted proxy service.", schema = @Schema(type = "string")),
                    @Parameter(in = ParameterIn.QUERY, name = "get_response_schema",
                            description = "Return JSON schema in response header or not.", schema = @Schema(type = "boolean")),
                    @Parameter(in = ParameterIn.QUERY, name = "json",
                            description = "Return in JSON format or not.", schema = @Schema(type = "boolean"))
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    encoding = @Encoding(contentType = "application/json"),
                                    schema = @Schema(name = "jsonState", example =
                                            "{\"summary\":{\"numIntraBrokerReplicaMovements\":0,\"numReplicaMovements\":22,\"onDemandBalancednessScoreAfter\":93.21842452909542,\"intraBrokerDataToMoveMB\":0,\"monitoredPartitionsPercentage\":100.0,\"provisionRecommendation\":\"\",\"excludedBrokersForReplicaMove\":[],\"excludedBrokersForLeadership\":[],\"provisionStatus\":\"RIGHT_SIZED\",\"onDemandBalancednessScoreBefore\":90.52689689611348,\"recentWindows\":5,\"dataToMoveMB\":0,\"excludedTopics\":[],\"numLeaderMovements\":2},\"goalSummary\":[{\"goal\":\"RackAwareDistributionGoal\",\"status\":\"NO-ACTION\",\"optimizationTimeMs\":0,\"clusterModelStats\":{\"metadata\":{\"topics\":3,\"brokers\":3,\"replicas\":129},\"statistics\":{\"AVG\":{\"disk\":0.6459576288859049,\"replicas\":43.0,\"leaderReplicas\":21.666666666666668,\"cpu\":0.09581887722015381,\"networkOutbound\":0.022673089988529682,\"networkInbound\":0.023853202660878498,\"topicReplicas\":14.333333333333334,\"potentialNwOut\":0.026638793702393432},\"STD\":{\"disk\":0.8268403579423366,\"replicas\":2.82842712474619,\"leaderReplicas\":2.6246692913372702,\"cpu\":0.0,\"networkOutbound\":0.0,\"networkInbound\":0.0,\"topicReplicas\":4.399775527382962,\"potentialNwOut\":0.022299485673227468},\"MIN\":{\"disk\":0.057915687561035156,\"replicas\":39,\"leaderReplicas\":18,\"cpu\":0.2874566316604614,\"networkOutbound\":0.06801926996558905,\"networkInbound\":0.0715596079826355,\"topicReplicas\":0,\"potentialNwOut\":0.008440279922607374},\"MAX\":{\"disk\":1.8152799606323242,\"replicas\":45,\"leaderReplicas\":24,\"cpu\":0.2874566316604614,\"networkOutbound\":0.06801926996558905,\"networkInbound\":0.0715596079826355,\"topicReplicas\":28,\"potentialNwOut\":0.06302393274381757}}}}]")
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Not found."),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error.")
            }
    )
    void rightsize(RoutingContext context);

    void destroy();
}
