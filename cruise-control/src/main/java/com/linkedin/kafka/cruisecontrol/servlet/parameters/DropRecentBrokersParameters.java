/*
 * Copyright 2019 LinkedIn Corp. Licensed under the BSD 2-Clause License (the "License"). See License in the project root for license information.
 */

package com.linkedin.kafka.cruisecontrol.servlet.parameters;

import com.linkedin.kafka.cruisecontrol.servlet.CruiseControlEndPoint;
import io.vertx.ext.web.RoutingContext;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static com.linkedin.kafka.cruisecontrol.servlet.parameters.ParameterUtils.*;


/**
 * Optional Parameters for {@link CruiseControlEndPoint#ADMIN}.
 * This class holds all the request parameters for {@link AdminParameters.AdminType#DROP_RECENT_BROKERS}.
 */
public class DropRecentBrokersParameters extends AbstractParameters {
  protected static final SortedSet<String> CASE_INSENSITIVE_PARAMETER_NAMES;
  protected static final String ADMIN = "ADMIN";
  static {
    SortedSet<String> validParameterNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    validParameterNames.add(DROP_RECENTLY_REMOVED_BROKERS_PARAM);
    validParameterNames.add(DROP_RECENTLY_DEMOTED_BROKERS_PARAM);
    validParameterNames.addAll(AbstractParameters.CASE_INSENSITIVE_PARAMETER_NAMES);
    CASE_INSENSITIVE_PARAMETER_NAMES = Collections.unmodifiableSortedSet(validParameterNames);
  }
  protected Set<Integer> _dropRecentlyRemovedBrokers;
  protected Set<Integer> _dropRecentlyDemotedBrokers;

  protected DropRecentBrokersParameters() {
    super();
  }

  @Override
  protected void initParameters() throws UnsupportedEncodingException {
    super.initParameters();
    _dropRecentlyRemovedBrokers = ParameterUtils.dropRecentlyRemovedBrokers(_request);
    _dropRecentlyDemotedBrokers = ParameterUtils.dropRecentlyDemotedBrokers(_request);
  }

  protected void initParameters(RoutingContext context) throws UnsupportedEncodingException {
    boolean json = Boolean.parseBoolean(context.queryParams().get(JSON_PARAM));
    super.initParameters(json, ADMIN);
    _dropRecentlyRemovedBrokers = ParameterUtils.dropRecentlyRemovedBrokers(context);
    _dropRecentlyDemotedBrokers = ParameterUtils.dropRecentlyDemotedBrokers(context);
  }

  /**
   * Create a {@link DropRecentBrokersParameters} object from the request.
   *
   * @param configs Information collected from request and Cruise Control configs.
   * @return A DropRecentBrokersParameters object; or null if any required parameter is not specified in the request.
   */
  public static DropRecentBrokersParameters maybeBuildDropRecentBrokersParameters(Map<String, ?> configs)
      throws UnsupportedEncodingException {
    DropRecentBrokersParameters dropRecentBrokersParameters = new DropRecentBrokersParameters();
    dropRecentBrokersParameters.configure(configs);
    dropRecentBrokersParameters.initParameters();
    // At least one recently removed/demoted broker should be specified to drop; otherwise, return null.
    if (dropRecentBrokersParameters.dropRecentlyDemotedBrokers().isEmpty()
        && dropRecentBrokersParameters.dropRecentlyRemovedBrokers().isEmpty()) {
      return null;
    }
    return dropRecentBrokersParameters;
  }

  /**
   * Create a {@link DropRecentBrokersParameters} object from the request.
   * @return A DropRecentBrokersParameters object; or null if any required parameter is not specified in the request.
   */
  public static DropRecentBrokersParameters maybeBuildDropRecentBrokersParameters(RoutingContext context)
          throws UnsupportedEncodingException {
    DropRecentBrokersParameters dropRecentBrokersParameters = new DropRecentBrokersParameters();
    dropRecentBrokersParameters.initParameters(context);
    // At least one recently removed/demoted broker should be specified to drop; otherwise, return null.
    if (dropRecentBrokersParameters.dropRecentlyDemotedBrokers().isEmpty()
            && dropRecentBrokersParameters.dropRecentlyRemovedBrokers().isEmpty()) {
      return null;
    }
    return dropRecentBrokersParameters;
  }

  public Set<Integer> dropRecentlyRemovedBrokers() {
    return _dropRecentlyRemovedBrokers;
  }

  public Set<Integer> dropRecentlyDemotedBrokers() {
    return _dropRecentlyDemotedBrokers;
  }

  @Override
  public SortedSet<String> caseInsensitiveParameterNames() {
    return CASE_INSENSITIVE_PARAMETER_NAMES;
  }
}
