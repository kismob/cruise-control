/*
 * Copyright 2018 LinkedIn Corp. Licensed under the BSD 2-Clause License (the "License"). See License in the project root for license information.
 */

package com.linkedin.kafka.cruisecontrol.vertx.parameters;

import com.linkedin.kafka.cruisecontrol.commonapi.CommonApi;
import com.linkedin.kafka.cruisecontrol.servlet.parameters.*;
import com.linkedin.kafka.cruisecontrol.servlet.response.CruiseControlState;
import io.vertx.core.MultiMap;
import io.vertx.ext.web.RoutingContext;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Locale;
import java.util.regex.Pattern;

import static com.linkedin.kafka.cruisecontrol.servlet.parameters.ParameterUtils.*;

public final class VertxParameterUtils {

    protected static final String PROPOSALS = "PROPOSALS";
    protected static final String VALID_WINDOWS = "VALID_WINDOWS";

    public static KafkaClusterStateParameters kafkaClusterStateParameters(MultiMap params) {
        String topic = params.get(TOPIC_PARAM) == null ? ".*" : params.get(TOPIC_PARAM);
        boolean verbose = Boolean.parseBoolean(params.get(VERBOSE_PARAM));
        boolean json = Boolean.parseBoolean(params.get(JSON_PARAM));
        KafkaClusterStateParameters kafkaClusterStateParameters = new KafkaClusterStateParameters();
        try {
            kafkaClusterStateParameters.initParameters(verbose, topic, json);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return kafkaClusterStateParameters;
    }

    public static CruiseControlStateParameters cruiseControlStateParameters(MultiMap params) {
        boolean verbose = Boolean.parseBoolean(params.get(VERBOSE_PARAM));
        boolean superVerbose = Boolean.parseBoolean(params.get(SUPER_VERBOSE_PARAM));
        boolean json = Boolean.parseBoolean(params.get(JSON_PARAM));
        ArrayList<String> subtateStrings = params.get(SUBSTATES_PARAM) == null
                ? null : new ArrayList<>(Arrays.asList(params.get(SUBSTATES_PARAM).split(",")));
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
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return cruiseControlStateParameters;
    }

    public static ClusterLoadParameters clusterLoadParameters(MultiMap params) {
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
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return clusterLoadParameters;
    }

    public static UserTasksParameters userTasksParameters(MultiMap params) {
        boolean json = Boolean.parseBoolean(params.get(JSON_PARAM));
        boolean fetchCompletedTask = Boolean.parseBoolean(params.get(FETCH_COMPLETED_TASK_PARAM));
        String userTaskIds = params.get(USER_TASK_IDS_PARAM) == null ? null : params.get(USER_TASK_IDS_PARAM);
        String clientIds = params.get(CLIENT_IDS_PARAM) == null ? null : params.get(CLIENT_IDS_PARAM);
        String entries = params.get(ENTRIES_PARAM) == null ? null : params.get(ENTRIES_PARAM);
        String endpoints = params.get(ENDPOINTS_PARAM) == null ? null : params.get(ENDPOINTS_PARAM);
        String types = params.get(TYPES_PARAM) == null ? null : params.get(TYPES_PARAM);
        UserTasksParameters userTasksParameters = new UserTasksParameters();
        try {
            userTasksParameters.initParameters(json, userTaskIds, clientIds, endpoints, types, entries, fetchCompletedTask);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return userTasksParameters;
    }

    public static PartitionLoadParameters partitionLoadParameters(MultiMap params) {
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
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return partitionLoadParameters;
    }

    public static ProposalsParameters proposalsParameters(MultiMap params) {
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
                    rebalanceDisk, json, verbose, fastMode, PROPOSALS);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return proposalsParameters;
    }

    public static RebalanceParameters rebalanceParameters(MultiMap params, RoutingContext context) {
        boolean dryRun = params.get(DRY_RUN_PARAM) == null || Boolean.parseBoolean(params.get(DRY_RUN_PARAM));
        Integer concurrentPartitionMovementsPerBroker = params.get(CONCURRENT_PARTITION_MOVEMENTS_PER_BROKER_PARAM) == null ?
                null : Integer.parseInt(params.get(CONCURRENT_PARTITION_MOVEMENTS_PER_BROKER_PARAM));
        Integer concurrentIntraBrokerPartitionMovements = params.get(CONCURRENT_INTRA_BROKER_PARTITION_MOVEMENTS_PARAM) == null ?
                null : Integer.parseInt(params.get(CONCURRENT_INTRA_BROKER_PARTITION_MOVEMENTS_PARAM));
        Integer concurrentLeaderMovements = params.get(CONCURRENT_LEADER_MOVEMENTS_PARAM) ==null ?
                null : Integer.parseInt(params.get(CONCURRENT_LEADER_MOVEMENTS_PARAM));
        Long executionProgressCheckIntervalMs = params.get(EXECUTION_PROGRESS_CHECK_INTERVAL_MS_PARAM) == null ?
                null : Long.parseLong(params.get(EXECUTION_PROGRESS_CHECK_INTERVAL_MS_PARAM));
        boolean skipHardGoalCheck = Boolean.parseBoolean(params.get(SKIP_HARD_GOAL_CHECK_PARAM));
        String replicaMovementStrategyString = params.get(REPLICA_MOVEMENT_STRATEGIES_PARAM);
        boolean ignoreProposalCache = Boolean.parseBoolean(params.get(IGNORE_PROPOSAL_CACHE_PARAM));
        boolean kafkaAssigner = Boolean.parseBoolean(params.get(KAFKA_ASSIGNER_MODE_PARAM));
        Long replicationThrottle = params.get(REPLICATION_THROTTLE_PARAM) == null ?
                null : Long.parseLong(params.get(REPLICATION_THROTTLE_PARAM));
        String reviewId = params.get(REVIEW_ID_PARAM);
        boolean rebalanceDisk = Boolean.parseBoolean(params.get(REBALANCE_DISK_MODE_PARAM));
        String reasonString = params.get(REASON_PARAM);
        String ipString = CommonApi.getVertxClientIpAddress(context);
        boolean stopOngoingExecution = Boolean.parseBoolean(params.get(STOP_ONGOING_EXECUTION_PARAM));
        String dataFrom = params.get(DATA_FROM_PARAM) == null ? "VALID_WINDOWS" : params.get(DATA_FROM_PARAM);
        String destinationBrokerIds = params.get(DESTINATION_BROKER_IDS_PARAM);
        String goals = params.get(GOALS_PARAM);
        boolean allowCapacityEstimation = params.get(ALLOW_CAPACITY_ESTIMATION_PARAM) == null || Boolean.parseBoolean(params.get(ALLOW_CAPACITY_ESTIMATION_PARAM));
        Pattern excludedTopics = params.get(EXCLUDED_TOPICS_PARAM) == null ? null : Pattern.compile(params.get(EXCLUDED_TOPICS_PARAM));
        boolean useReadyDefaultGoals = Boolean.parseBoolean(params.get(USE_READY_DEFAULT_GOALS_PARAM));
        boolean excludeRecentlyDemotedBrokers = Boolean.parseBoolean(params.get(EXCLUDE_RECENTLY_DEMOTED_BROKERS_PARAM));
        boolean excludeRecentlyRemovedBrokers = Boolean.parseBoolean(params.get(EXCLUDE_RECENTLY_REMOVED_BROKERS_PARAM));
        boolean json = Boolean.parseBoolean(params.get(JSON_PARAM));
        boolean verbose = Boolean.parseBoolean(params.get(VERBOSE_PARAM));
        boolean fastMode = params.get(FAST_MODE_PARAM) == null || Boolean.parseBoolean(params.get(FAST_MODE_PARAM));
        RebalanceParameters rebalanceParameters = new RebalanceParameters();
        try {
            rebalanceParameters.initParameters(dryRun, concurrentPartitionMovementsPerBroker, concurrentIntraBrokerPartitionMovements,
                    concurrentLeaderMovements, executionProgressCheckIntervalMs, skipHardGoalCheck, replicaMovementStrategyString, ignoreProposalCache,
                    destinationBrokerIds, kafkaAssigner, replicationThrottle, reviewId, params, rebalanceDisk, reasonString, ipString, stopOngoingExecution,
                    dataFrom, goals, allowCapacityEstimation, excludedTopics, useReadyDefaultGoals, excludeRecentlyDemotedBrokers, excludeRecentlyRemovedBrokers,
                    json, verbose, fastMode);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return rebalanceParameters;
    }

    public static AddBrokerParameters addBrokerParameters(MultiMap params, RoutingContext context) {
        boolean throttleAddedBroker = Boolean.parseBoolean(params.get(THROTTLE_ADDED_BROKER_PARAM));
        String brokerIdString = params.get(BROKER_ID_PARAM);
        boolean dryRun = params.get(DRY_RUN_PARAM) == null || Boolean.parseBoolean(params.get(DRY_RUN_PARAM));
        Integer concurrentPartitionMovementsPerBroker = params.get(CONCURRENT_PARTITION_MOVEMENTS_PER_BROKER_PARAM) == null ?
                null : Integer.parseInt(params.get(CONCURRENT_PARTITION_MOVEMENTS_PER_BROKER_PARAM));

        Integer concurrentLeaderMovements = params.get(CONCURRENT_LEADER_MOVEMENTS_PARAM) ==null ?
                null : Integer.parseInt(params.get(CONCURRENT_LEADER_MOVEMENTS_PARAM));
        Long executionProgressCheckIntervalMs = params.get(EXECUTION_PROGRESS_CHECK_INTERVAL_MS_PARAM) == null ?
                null : Long.parseLong(params.get(EXECUTION_PROGRESS_CHECK_INTERVAL_MS_PARAM));
        boolean skipHardGoalCheck = Boolean.parseBoolean(params.get(SKIP_HARD_GOAL_CHECK_PARAM));
        String replicaMovementStrategyString = params.get(REPLICA_MOVEMENT_STRATEGIES_PARAM);
        boolean kafkaAssigner = Boolean.parseBoolean(params.get(KAFKA_ASSIGNER_MODE_PARAM));
        Long replicationThrottle = params.get(REPLICATION_THROTTLE_PARAM) == null ?
                null : Long.parseLong(params.get(REPLICATION_THROTTLE_PARAM));
        String reviewId = params.get(REVIEW_ID_PARAM);
        boolean rebalanceDisk = Boolean.parseBoolean(params.get(REBALANCE_DISK_MODE_PARAM));
        String reasonString = params.get(REASON_PARAM);
        String ipString = CommonApi.getVertxClientIpAddress(context);
        boolean stopOngoingExecution = Boolean.parseBoolean(params.get(STOP_ONGOING_EXECUTION_PARAM));
        String dataFrom = params.get(DATA_FROM_PARAM) == null ? VALID_WINDOWS : params.get(DATA_FROM_PARAM);
        String goals = params.get(GOALS_PARAM);
        boolean allowCapacityEstimation = params.get(ALLOW_CAPACITY_ESTIMATION_PARAM) == null || Boolean.parseBoolean(params.get(ALLOW_CAPACITY_ESTIMATION_PARAM));
        Pattern excludedTopics = params.get(EXCLUDED_TOPICS_PARAM) == null ? null : Pattern.compile(params.get(EXCLUDED_TOPICS_PARAM));
        boolean useReadyDefaultGoals = Boolean.parseBoolean(params.get(USE_READY_DEFAULT_GOALS_PARAM));
        boolean excludeRecentlyDemotedBrokers = Boolean.parseBoolean(params.get(EXCLUDE_RECENTLY_DEMOTED_BROKERS_PARAM));
        boolean excludeRecentlyRemovedBrokers = Boolean.parseBoolean(params.get(EXCLUDE_RECENTLY_REMOVED_BROKERS_PARAM));
        boolean json = Boolean.parseBoolean(params.get(JSON_PARAM));
        boolean verbose = Boolean.parseBoolean(params.get(VERBOSE_PARAM));
        boolean fastMode = params.get(FAST_MODE_PARAM) == null || Boolean.parseBoolean(params.get(FAST_MODE_PARAM));
        AddBrokerParameters addBrokerParameters = new AddBrokerParameters();
        try {
            addBrokerParameters.initParameters(throttleAddedBroker, brokerIdString, context, dryRun, concurrentPartitionMovementsPerBroker, concurrentLeaderMovements, executionProgressCheckIntervalMs,
                    replicationThrottle, skipHardGoalCheck, replicaMovementStrategyString, reviewId, reasonString, ipString, stopOngoingExecution, dataFrom, goals, kafkaAssigner, rebalanceDisk, allowCapacityEstimation, excludedTopics, useReadyDefaultGoals,
                    excludeRecentlyDemotedBrokers, excludeRecentlyRemovedBrokers, json, verbose, fastMode);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return addBrokerParameters;
    }

    public static RemoveBrokerParameters removeBrokerParameters(MultiMap params, RoutingContext context) {
        boolean throttleRemovedBroker = Boolean.parseBoolean(params.get(THROTTLE_ADDED_BROKER_PARAM));
        String brokerIdString = params.get(BROKER_ID_PARAM);
        boolean dryRun = params.get(DRY_RUN_PARAM) == null || Boolean.parseBoolean(params.get(DRY_RUN_PARAM));
        Integer concurrentPartitionMovementsPerBroker = params.get(CONCURRENT_PARTITION_MOVEMENTS_PER_BROKER_PARAM) == null ?
                null : Integer.parseInt(params.get(CONCURRENT_PARTITION_MOVEMENTS_PER_BROKER_PARAM));

        Integer concurrentLeaderMovements = params.get(CONCURRENT_LEADER_MOVEMENTS_PARAM) ==null ?
                null : Integer.parseInt(params.get(CONCURRENT_LEADER_MOVEMENTS_PARAM));
        Long executionProgressCheckIntervalMs = params.get(EXECUTION_PROGRESS_CHECK_INTERVAL_MS_PARAM) == null ?
                null : Long.parseLong(params.get(EXECUTION_PROGRESS_CHECK_INTERVAL_MS_PARAM));
        boolean skipHardGoalCheck = Boolean.parseBoolean(params.get(SKIP_HARD_GOAL_CHECK_PARAM));
        String replicaMovementStrategyString = params.get(REPLICA_MOVEMENT_STRATEGIES_PARAM);
        boolean kafkaAssigner = Boolean.parseBoolean(params.get(KAFKA_ASSIGNER_MODE_PARAM));
        Long replicationThrottle = params.get(REPLICATION_THROTTLE_PARAM) == null ?
                null : Long.parseLong(params.get(REPLICATION_THROTTLE_PARAM));
        String reviewId = params.get(REVIEW_ID_PARAM);
        boolean rebalanceDisk = Boolean.parseBoolean(params.get(REBALANCE_DISK_MODE_PARAM));
        String reasonString = params.get(REASON_PARAM);
        String ipString = CommonApi.getVertxClientIpAddress(context);
        boolean stopOngoingExecution = Boolean.parseBoolean(params.get(STOP_ONGOING_EXECUTION_PARAM));
        String dataFrom = params.get(DATA_FROM_PARAM) == null ? VALID_WINDOWS : params.get(DATA_FROM_PARAM);
        String goals = params.get(GOALS_PARAM);
        boolean allowCapacityEstimation = params.get(ALLOW_CAPACITY_ESTIMATION_PARAM) == null || Boolean.parseBoolean(params.get(ALLOW_CAPACITY_ESTIMATION_PARAM));
        Pattern excludedTopics = params.get(EXCLUDED_TOPICS_PARAM) == null ? null : Pattern.compile(params.get(EXCLUDED_TOPICS_PARAM));
        boolean useReadyDefaultGoals = Boolean.parseBoolean(params.get(USE_READY_DEFAULT_GOALS_PARAM));
        boolean excludeRecentlyDemotedBrokers = Boolean.parseBoolean(params.get(EXCLUDE_RECENTLY_DEMOTED_BROKERS_PARAM));
        boolean excludeRecentlyRemovedBrokers = Boolean.parseBoolean(params.get(EXCLUDE_RECENTLY_REMOVED_BROKERS_PARAM));
        boolean json = Boolean.parseBoolean(params.get(JSON_PARAM));
        boolean verbose = Boolean.parseBoolean(params.get(VERBOSE_PARAM));
        boolean fastMode = params.get(FAST_MODE_PARAM) == null || Boolean.parseBoolean(params.get(FAST_MODE_PARAM));
        String destinationBrokerIdsString = params.get(DESTINATION_BROKER_IDS_PARAM);
        RemoveBrokerParameters removeBrokerParameters = new RemoveBrokerParameters();
        try {
            removeBrokerParameters.initParameters(throttleRemovedBroker, brokerIdString, context, dryRun, concurrentPartitionMovementsPerBroker, concurrentLeaderMovements, executionProgressCheckIntervalMs,
                    replicationThrottle, skipHardGoalCheck, replicaMovementStrategyString, reviewId, reasonString, ipString, stopOngoingExecution, dataFrom, goals, kafkaAssigner, rebalanceDisk, allowCapacityEstimation, excludedTopics, useReadyDefaultGoals,
                    excludeRecentlyDemotedBrokers, excludeRecentlyRemovedBrokers, json, verbose, fastMode, destinationBrokerIdsString);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return removeBrokerParameters;
    }

    public static FixOfflineReplicasParameters fixOfflineReplicasParameters(MultiMap params, RoutingContext context) {
        boolean dryRun = params.get(DRY_RUN_PARAM) == null || Boolean.parseBoolean(params.get(DRY_RUN_PARAM));
        Integer concurrentPartitionMovementsPerBroker = params.get(CONCURRENT_PARTITION_MOVEMENTS_PER_BROKER_PARAM) == null ?
                null : Integer.parseInt(params.get(CONCURRENT_PARTITION_MOVEMENTS_PER_BROKER_PARAM));

        Integer concurrentLeaderMovements = params.get(CONCURRENT_LEADER_MOVEMENTS_PARAM) ==null ?
                null : Integer.parseInt(params.get(CONCURRENT_LEADER_MOVEMENTS_PARAM));
        Long executionProgressCheckIntervalMs = params.get(EXECUTION_PROGRESS_CHECK_INTERVAL_MS_PARAM) == null ?
                null : Long.parseLong(params.get(EXECUTION_PROGRESS_CHECK_INTERVAL_MS_PARAM));
        boolean skipHardGoalCheck = Boolean.parseBoolean(params.get(SKIP_HARD_GOAL_CHECK_PARAM));
        String replicaMovementStrategyString = params.get(REPLICA_MOVEMENT_STRATEGIES_PARAM);
        boolean kafkaAssigner = Boolean.parseBoolean(params.get(KAFKA_ASSIGNER_MODE_PARAM));
        Long replicationThrottle = params.get(REPLICATION_THROTTLE_PARAM) == null ?
                null : Long.parseLong(params.get(REPLICATION_THROTTLE_PARAM));
        String reviewId = params.get(REVIEW_ID_PARAM);
        boolean rebalanceDisk = Boolean.parseBoolean(params.get(REBALANCE_DISK_MODE_PARAM));
        String reasonString = params.get(REASON_PARAM);
        String ipString = CommonApi.getVertxClientIpAddress(context);
        boolean stopOngoingExecution = Boolean.parseBoolean(params.get(STOP_ONGOING_EXECUTION_PARAM));
        String dataFrom = params.get(DATA_FROM_PARAM) == null ? VALID_WINDOWS : params.get(DATA_FROM_PARAM);
        String goals = params.get(GOALS_PARAM);
        boolean allowCapacityEstimation = params.get(ALLOW_CAPACITY_ESTIMATION_PARAM) == null || Boolean.parseBoolean(params.get(ALLOW_CAPACITY_ESTIMATION_PARAM));
        Pattern excludedTopics = params.get(EXCLUDED_TOPICS_PARAM) == null ? null : Pattern.compile(params.get(EXCLUDED_TOPICS_PARAM));
        boolean useReadyDefaultGoals = Boolean.parseBoolean(params.get(USE_READY_DEFAULT_GOALS_PARAM));
        boolean excludeRecentlyDemotedBrokers = Boolean.parseBoolean(params.get(EXCLUDE_RECENTLY_DEMOTED_BROKERS_PARAM));
        boolean excludeRecentlyRemovedBrokers = Boolean.parseBoolean(params.get(EXCLUDE_RECENTLY_REMOVED_BROKERS_PARAM));
        boolean json = Boolean.parseBoolean(params.get(JSON_PARAM));
        boolean verbose = Boolean.parseBoolean(params.get(VERBOSE_PARAM));
        boolean fastMode = params.get(FAST_MODE_PARAM) == null || Boolean.parseBoolean(params.get(FAST_MODE_PARAM));
        FixOfflineReplicasParameters fixOfflineReplicasParameters = new FixOfflineReplicasParameters();
        try {
            fixOfflineReplicasParameters.initParameters(context, dryRun, concurrentPartitionMovementsPerBroker, concurrentLeaderMovements, executionProgressCheckIntervalMs,
                    replicationThrottle, skipHardGoalCheck, replicaMovementStrategyString, reviewId, reasonString, ipString, stopOngoingExecution, dataFrom, goals, kafkaAssigner, rebalanceDisk, allowCapacityEstimation, excludedTopics, useReadyDefaultGoals,
                    excludeRecentlyDemotedBrokers, excludeRecentlyRemovedBrokers, json, verbose, fastMode);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return fixOfflineReplicasParameters;
    }

    public static DemoteBrokerParameters demoteBrokerParameters(MultiMap params, RoutingContext context) {
        boolean skipUrpDemotion = params.get(SKIP_URP_DEMOTION_PARAM) == null || Boolean.parseBoolean(params.get(SKIP_URP_DEMOTION_PARAM));
        boolean dryRun = params.get(DRY_RUN_PARAM) == null || Boolean.parseBoolean(params.get(DRY_RUN_PARAM));
        boolean excludeFollowerDemotion = params.get(EXCLUDE_FOLLOWER_DEMOTION_PARAM) == null || Boolean.parseBoolean(params.get(EXCLUDE_FOLLOWER_DEMOTION_PARAM));
        Integer concurrentLeaderMovements = params.get(CONCURRENT_LEADER_MOVEMENTS_PARAM) ==null ?
                null : Integer.parseInt(params.get(CONCURRENT_LEADER_MOVEMENTS_PARAM));
        Long executionProgressCheckIntervalMs = params.get(EXECUTION_PROGRESS_CHECK_INTERVAL_MS_PARAM) == null ?
                null : Long.parseLong(params.get(EXECUTION_PROGRESS_CHECK_INTERVAL_MS_PARAM));
        String replicaMovementStrategyString = params.get(REPLICA_MOVEMENT_STRATEGIES_PARAM);
        Long replicationThrottle = params.get(REPLICATION_THROTTLE_PARAM) == null ?
                null : Long.parseLong(params.get(REPLICATION_THROTTLE_PARAM));
        String reviewId = params.get(REVIEW_ID_PARAM);
        String reasonString = params.get(REASON_PARAM);
        String ipString = CommonApi.getVertxClientIpAddress(context);
        boolean stopOngoingExecution = Boolean.parseBoolean(params.get(STOP_ONGOING_EXECUTION_PARAM));
        boolean allowCapacityEstimation = params.get(ALLOW_CAPACITY_ESTIMATION_PARAM) == null || Boolean.parseBoolean(params.get(ALLOW_CAPACITY_ESTIMATION_PARAM));
        boolean excludeRecentlyDemotedBrokers = Boolean.parseBoolean(params.get(EXCLUDE_RECENTLY_DEMOTED_BROKERS_PARAM));
        boolean json = Boolean.parseBoolean(params.get(JSON_PARAM));
        boolean verbose = Boolean.parseBoolean(params.get(VERBOSE_PARAM));
        String brokerIdAndLogdirsString = params.get(BROKER_ID_AND_LOGDIRS_PARAM);
        String brokerIdsString = params.get(BROKER_ID_PARAM);
        DemoteBrokerParameters demoteBrokerParameters = new DemoteBrokerParameters();
        try {
            demoteBrokerParameters.initParameters(context, brokerIdsString, dryRun, concurrentLeaderMovements, executionProgressCheckIntervalMs,
            allowCapacityEstimation, skipUrpDemotion, excludeFollowerDemotion, replicaMovementStrategyString, replicationThrottle,
                    reviewId, brokerIdAndLogdirsString, reasonString, ipString, stopOngoingExecution, excludeRecentlyDemotedBrokers,
            json, verbose);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return demoteBrokerParameters;
    }

    public static StopProposalParameters stopProposalExecutionParameters(MultiMap params, RoutingContext context) {
        boolean forceStop = Boolean.parseBoolean(params.get(FORCE_STOP_PARAM));
        boolean stopExternalAgent = params.get(STOP_EXTERNAL_AGENT_PARAM) == null || Boolean.parseBoolean(params.get(STOP_EXTERNAL_AGENT_PARAM));
        boolean json = Boolean.parseBoolean(params.get(JSON_PARAM));
        StopProposalParameters stopProposalParameters = new StopProposalParameters();
        try{
            stopProposalParameters.initParameters(forceStop, stopExternalAgent, json, context);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return stopProposalParameters;
    }

    public static PauseResumeParameters pauseResumeParameters(MultiMap params, RoutingContext context, String endpointName){
        boolean json = Boolean.parseBoolean(params.get(JSON_PARAM));
        String ipString = CommonApi.getVertxClientIpAddress(context);
        String reason = params.get(REASON_PARAM);
        PauseResumeParameters pauseResumeParameters = new PauseResumeParameters();
        try {
            pauseResumeParameters.initParameters(reason, ipString, context, json, endpointName);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return  pauseResumeParameters;
    }

    public static TopicConfigurationParameters topicConfigurationParameters(MultiMap params, RoutingContext context) {
        boolean dryRun = params.get(DRY_RUN_PARAM) == null || Boolean.parseBoolean(params.get(DRY_RUN_PARAM));
        Integer concurrentLeaderMovements = params.get(CONCURRENT_LEADER_MOVEMENTS_PARAM) ==null ?
                null : Integer.parseInt(params.get(CONCURRENT_LEADER_MOVEMENTS_PARAM));
        Long executionProgressCheckIntervalMs = params.get(EXECUTION_PROGRESS_CHECK_INTERVAL_MS_PARAM) == null ?
                null : Long.parseLong(params.get(EXECUTION_PROGRESS_CHECK_INTERVAL_MS_PARAM));
        boolean skipHardGoalCheck = Boolean.parseBoolean(params.get(SKIP_HARD_GOAL_CHECK_PARAM));
        String replicaMovementStrategyString = params.get(REPLICA_MOVEMENT_STRATEGIES_PARAM);
        boolean kafkaAssigner = Boolean.parseBoolean(params.get(KAFKA_ASSIGNER_MODE_PARAM));
        boolean skipRackAwarenessCheck = Boolean.parseBoolean(params.get(SKIP_RACK_AWARENESS_CHECK_PARAM));
        Long replicationThrottle = params.get(REPLICATION_THROTTLE_PARAM) == null ?
                null : Long.parseLong(params.get(REPLICATION_THROTTLE_PARAM));
        boolean rebalanceDisk = Boolean.parseBoolean(params.get(REBALANCE_DISK_MODE_PARAM));
        String reasonString = params.get(REASON_PARAM);
        String ipString = CommonApi.getVertxClientIpAddress(context);
        boolean stopOngoingExecution = Boolean.parseBoolean(params.get(STOP_ONGOING_EXECUTION_PARAM));
        String dataFrom = params.get(DATA_FROM_PARAM) == null ? VALID_WINDOWS : params.get(DATA_FROM_PARAM);
        String goals = params.get(GOALS_PARAM);
        boolean allowCapacityEstimation = params.get(ALLOW_CAPACITY_ESTIMATION_PARAM) == null || Boolean.parseBoolean(params.get(ALLOW_CAPACITY_ESTIMATION_PARAM));
        Pattern excludedTopics = params.get(EXCLUDED_TOPICS_PARAM) == null ? null : Pattern.compile(params.get(EXCLUDED_TOPICS_PARAM));
        boolean useReadyDefaultGoals = Boolean.parseBoolean(params.get(USE_READY_DEFAULT_GOALS_PARAM));
        boolean excludeRecentlyDemotedBrokers = Boolean.parseBoolean(params.get(EXCLUDE_RECENTLY_DEMOTED_BROKERS_PARAM));
        boolean excludeRecentlyRemovedBrokers = Boolean.parseBoolean(params.get(EXCLUDE_RECENTLY_REMOVED_BROKERS_PARAM));
        boolean json = Boolean.parseBoolean(params.get(JSON_PARAM));
        boolean verbose = Boolean.parseBoolean(params.get(VERBOSE_PARAM));
        boolean fastMode = params.get(FAST_MODE_PARAM) == null || Boolean.parseBoolean(params.get(FAST_MODE_PARAM));
        TopicConfigurationParameters topicConfigurationParameters = new TopicConfigurationParameters();
        try {
            topicConfigurationParameters.initParameters(context, dryRun, reasonString, ipString, stopOngoingExecution, skipRackAwarenessCheck,
                    concurrentLeaderMovements, executionProgressCheckIntervalMs, skipHardGoalCheck, replicaMovementStrategyString, replicationThrottle,
                    json, dataFrom, goals, kafkaAssigner, rebalanceDisk, allowCapacityEstimation, excludedTopics, useReadyDefaultGoals, excludeRecentlyDemotedBrokers, excludeRecentlyRemovedBrokers,
                    verbose, fastMode);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return topicConfigurationParameters;
    }

    public static AdminParameters adminParameters(RoutingContext context) {
        AdminParameters adminParameters = new AdminParameters();
        try {
            adminParameters.initParameters(context);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return adminParameters;
    }

    public static RightsizeParameters rightsizeParameters(RoutingContext context) {
        MultiMap params = context.queryParams();
        String numBrokerString = params.get(NUM_BROKERS_TO_ADD);
        String partitionCountString = params.get(PARTITION_COUNT);
        boolean json = Boolean.parseBoolean(params.get(JSON_PARAM));
        RightsizeParameters rightsizeParameters = new RightsizeParameters();
        try {
            rightsizeParameters.initParameters(numBrokerString, partitionCountString, context, json);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return  rightsizeParameters;
    }
}
