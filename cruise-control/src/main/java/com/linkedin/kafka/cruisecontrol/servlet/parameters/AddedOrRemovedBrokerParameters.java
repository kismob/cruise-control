/*
 * Copyright 2018 LinkedIn Corp. Licensed under the BSD 2-Clause License (the "License"). See License in the project root for license information.
 */

package com.linkedin.kafka.cruisecontrol.servlet.parameters;

import com.linkedin.kafka.cruisecontrol.config.constants.ExecutorConfig;
import com.linkedin.kafka.cruisecontrol.config.constants.WebServerConfig;
import com.linkedin.kafka.cruisecontrol.executor.strategy.ReplicaMovementStrategy;
import com.linkedin.kafka.cruisecontrol.servlet.UserRequestException;
import io.vertx.ext.web.RoutingContext;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Set;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

import static com.linkedin.kafka.cruisecontrol.servlet.parameters.ParameterUtils.*;

public abstract class AddedOrRemovedBrokerParameters extends GoalBasedOptimizationParameters {
  protected static final SortedSet<String> CASE_INSENSITIVE_PARAMETER_NAMES;
  static {
    SortedSet<String> validParameterNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    validParameterNames.add(KAFKA_ASSIGNER_MODE_PARAM);
    validParameterNames.add(BROKER_ID_PARAM);
    validParameterNames.add(CONCURRENT_PARTITION_MOVEMENTS_PER_BROKER_PARAM);
    validParameterNames.add(CONCURRENT_LEADER_MOVEMENTS_PARAM);
    validParameterNames.add(EXECUTION_PROGRESS_CHECK_INTERVAL_MS_PARAM);
    validParameterNames.add(DRY_RUN_PARAM);
    validParameterNames.add(REASON_PARAM);
    validParameterNames.add(REPLICATION_THROTTLE_PARAM);
    validParameterNames.add(SKIP_HARD_GOAL_CHECK_PARAM);
    validParameterNames.add(REPLICA_MOVEMENT_STRATEGIES_PARAM);
    validParameterNames.add(REVIEW_ID_PARAM);
    validParameterNames.add(STOP_ONGOING_EXECUTION_PARAM);
    validParameterNames.addAll(GoalBasedOptimizationParameters.CASE_INSENSITIVE_PARAMETER_NAMES);
    CASE_INSENSITIVE_PARAMETER_NAMES = Collections.unmodifiableSortedSet(validParameterNames);
  }
  protected Set<Integer> _brokerIds;
  protected Integer _concurrentInterBrokerPartitionMovements;
  protected Integer _concurrentLeaderMovements;
  protected Long _executionProgressCheckIntervalMs;
  protected boolean _dryRun;
  protected Long _replicationThrottle;
  protected boolean _skipHardGoalCheck;
  protected ReplicaMovementStrategy _replicaMovementStrategy;
  protected Integer _reviewId;
  protected String _reason;
  protected boolean _stopOngoingExecution;

  public AddedOrRemovedBrokerParameters() {
    super();
  }

  @Override
  protected void initParameters() throws UnsupportedEncodingException {
    super.initParameters();
    _brokerIds = ParameterUtils.brokerIds(_request, false);
    _dryRun = ParameterUtils.getDryRun(_request);
    _concurrentInterBrokerPartitionMovements = ParameterUtils.concurrentMovements(_request, true, false);
    _concurrentLeaderMovements = ParameterUtils.concurrentMovements(_request, false, false);
    _executionProgressCheckIntervalMs = ParameterUtils.executionProgressCheckIntervalMs(_request);
    _replicationThrottle = ParameterUtils.replicationThrottle(_request, _config);
    _skipHardGoalCheck = ParameterUtils.skipHardGoalCheck(_request);
    _replicaMovementStrategy = ParameterUtils.getReplicaMovementStrategy(_request, _config);
    boolean twoStepVerificationEnabled = _config.getBoolean(WebServerConfig.TWO_STEP_VERIFICATION_ENABLED_CONFIG);
    _reviewId = ParameterUtils.reviewId(_request, twoStepVerificationEnabled);
    boolean requestReasonRequired = _config.getBoolean(ExecutorConfig.REQUEST_REASON_REQUIRED_CONFIG);
    _reason = ParameterUtils.reason(_request, requestReasonRequired && !_dryRun);
    _stopOngoingExecution = ParameterUtils.stopOngoingExecution(_request);
    if (_stopOngoingExecution && _dryRun) {
      throw new UserRequestException(String.format("%s and %s cannot both be set to true.", STOP_ONGOING_EXECUTION_PARAM, DRY_RUN_PARAM));
    }
  }

  protected void initParameters(String brokerIdsString, RoutingContext context, boolean dryRun, Integer concurrentInterBrokerPartitionMovements,
                                Integer concurrentLeaderMovements, Long executionProgressCheckIntervalMs, Long replicationThrottle,
                                boolean skipHardGoalCheck, String replicaMovementStrategyString, String reviewIdString, String reasonString,
                                String ipString, boolean stopOngoingExecution, String dataFrom, String inGoals, boolean kafkaAssigner,
                                boolean rebalanceDisk, boolean allowCapacityEstimation, Pattern excludedTopics, boolean useReadyDefaultGoals,
                                boolean excludeRecentlyDemotedBrokers, boolean excludeRecentlyRemovedBrokers,
                                boolean json, boolean verbose, boolean fastMode, String endpointName) throws UnsupportedEncodingException {
    super.initParameters(dataFrom, inGoals, kafkaAssigner, rebalanceDisk, allowCapacityEstimation, excludedTopics, useReadyDefaultGoals,
            excludeRecentlyDemotedBrokers, excludeRecentlyRemovedBrokers, json, verbose, fastMode, endpointName);
    _brokerIds = ParameterUtils.brokerIds(brokerIdsString, false, context);
    _dryRun = dryRun;
    _concurrentInterBrokerPartitionMovements = concurrentInterBrokerPartitionMovements;
    _concurrentLeaderMovements = concurrentLeaderMovements;
    _executionProgressCheckIntervalMs = executionProgressCheckIntervalMs;
    _replicationThrottle = replicationThrottle;
    _skipHardGoalCheck = skipHardGoalCheck;
    _replicaMovementStrategy = ParameterUtils.getReplicaMovementStrategy(dryRun, replicaMovementStrategyString, _config);
    boolean twoStepVerificationEnabled = false;
    _reviewId = ParameterUtils.reviewId(reviewIdString, twoStepVerificationEnabled, context.queryParams());
    boolean requestReasonRequired = false;
    _reason = ParameterUtils.reason(reasonString, requestReasonRequired && !_dryRun, ipString);
    _stopOngoingExecution = stopOngoingExecution;
    if (_stopOngoingExecution && _dryRun) {
      throw new UserRequestException(String.format("%s and %s cannot both be set to true.", STOP_ONGOING_EXECUTION_PARAM, DRY_RUN_PARAM));
    }
  }

  @Override
  public void setReviewId(int reviewId) {
    _reviewId = reviewId;
  }

  public Integer reviewId() {
    return _reviewId;
  }

  public Set<Integer> brokerIds() {
    return _brokerIds;
  }

  public Integer concurrentInterBrokerPartitionMovements() {
    return _concurrentInterBrokerPartitionMovements;
  }

  public Long executionProgressCheckIntervalMs() {
    return _executionProgressCheckIntervalMs;
  }

  public Integer concurrentLeaderMovements() {
    return _concurrentLeaderMovements;
  }

  public boolean dryRun() {
    return _dryRun;
  }

  public Long replicationThrottle() {
    return _replicationThrottle;
  }

  public boolean skipHardGoalCheck() {
    return _skipHardGoalCheck;
  }

  public ReplicaMovementStrategy replicaMovementStrategy() {
    return _replicaMovementStrategy;
  }

  public String reason() {
    return _reason;
  }

  public boolean stopOngoingExecution() {
    return _stopOngoingExecution;
  }

  @Override
  public void configure(Map<String, ?> configs) {
    super.configure(configs);
  }
}
