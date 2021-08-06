/*
 * Copyright 2018 LinkedIn Corp. Licensed under the BSD 2-Clause License (the "License"). See License in the project root for license information.
 */

package com.linkedin.kafka.cruisecontrol.servlet.parameters;

import com.linkedin.kafka.cruisecontrol.servlet.CruiseControlEndPoint;
import com.linkedin.kafka.cruisecontrol.servlet.UserTaskManager;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;

import static com.linkedin.kafka.cruisecontrol.servlet.parameters.ParameterUtils.*;


/**
 * Parameters for {@link CruiseControlEndPoint#USER_TASKS}
 *
 * <pre>
 * Retrieve the recent user tasks.
 *    GET /kafkacruisecontrol/user_tasks?json=[true/false]&amp;user_task_ids=[Set-of-USER-TASK-IDS]&amp;client_ids=[Set-of-ClientIdentity]&amp;
 *    endpoints=[Set-of-{@link CruiseControlEndPoint}]&amp;types=[Set-of-{@link UserTaskManager.TaskState}]&amp;entries=[POSITIVE-INTEGER]
 *    &amp;fetch_completed_task=[true/false]&amp;get_response_schema=[true/false]&amp;doAs=[user]
 * </pre>
 */
public class UserTasksParameters extends AbstractParameters {
  protected static final String USER_TASKS = "USER_TASKS";

  protected static final SortedSet<String> CASE_INSENSITIVE_PARAMETER_NAMES;
  static {
    SortedSet<String> validParameterNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    validParameterNames.add(USER_TASK_IDS_PARAM);
    validParameterNames.add(CLIENT_IDS_PARAM);
    validParameterNames.add(ENDPOINTS_PARAM);
    validParameterNames.add(TYPES_PARAM);
    validParameterNames.add(ENTRIES_PARAM);
    validParameterNames.add(FETCH_COMPLETED_TASK_PARAM);
    validParameterNames.addAll(AbstractParameters.CASE_INSENSITIVE_PARAMETER_NAMES);
    CASE_INSENSITIVE_PARAMETER_NAMES = Collections.unmodifiableSortedSet(validParameterNames);
  }
  protected Set<UUID> _userTaskIds;
  protected Set<String> _clientIds;
  protected Set<CruiseControlEndPoint> _endPoints;
  protected Set<UserTaskManager.TaskState> _types;
  protected int _entries;
  protected boolean _fetchCompletedTask;

  public UserTasksParameters() {
    super();
  }

  @Override
  protected void initParameters() throws UnsupportedEncodingException {
    super.initParameters();
    _userTaskIds = ParameterUtils.userTaskIds(_request);
    _clientIds = ParameterUtils.clientIds(_request);
    _endPoints = ParameterUtils.endPoints(_request);
    _types = ParameterUtils.types(_request);
    _entries = ParameterUtils.entries(_request);
    _fetchCompletedTask = ParameterUtils.fetchCompletedTask(_request);
  }

  public void initParameters(boolean json, String user_task_ids_string, String client_ids_string, String end_points_string,
                                     String types_string, String entries_string, boolean fetch_completed_task) throws UnsupportedEncodingException {
    super.initParameters(json, USER_TASKS);
    _userTaskIds = ParameterUtils.userTaskIds(user_task_ids_string);
    _clientIds = ParameterUtils.clientIds(client_ids_string);
    _endPoints = ParameterUtils.endPoints(end_points_string);
    _types = ParameterUtils.types(types_string);
    _entries = ParameterUtils.entries(entries_string);
    _fetchCompletedTask = fetch_completed_task;
  }

  public Set<UUID> userTaskIds() {
    return Collections.unmodifiableSet(_userTaskIds);
  }

  public Set<String> clientIds() {
    return Collections.unmodifiableSet(_clientIds);
  }

  public Set<CruiseControlEndPoint> endPoints() {
    return Collections.unmodifiableSet(_endPoints);
  }

  public Set<UserTaskManager.TaskState> types() {
    return Collections.unmodifiableSet(_types);
  }

  public int entries() {
    return _entries;
  }

  public boolean fetchCompletedTask() {
    return _fetchCompletedTask;
  }

  @Override
  public void configure(Map<String, ?> configs) {
    super.configure(configs);
  }

  @Override
  public SortedSet<String> caseInsensitiveParameterNames() {
    return CASE_INSENSITIVE_PARAMETER_NAMES;
  }
}
