/*
 * Copyright 2019 LinkedIn Corp. Licensed under the BSD 2-Clause License (the "License"). See License in the project root for license information.
 */

package com.linkedin.kafka.cruisecontrol.servlet.handler;

import com.linkedin.cruisecontrol.servlet.handler.Request;
import com.linkedin.cruisecontrol.servlet.parameters.CruiseControlParameters;
import com.linkedin.cruisecontrol.servlet.response.CruiseControlResponse;
import com.linkedin.kafka.cruisecontrol.servlet.KafkaCruiseControlServlet;
import com.linkedin.kafka.cruisecontrol.vertx.EndPoints;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

import static com.linkedin.cruisecontrol.common.utils.Utils.validateNotNull;
import static com.linkedin.kafka.cruisecontrol.servlet.KafkaCruiseControlServletUtils.KAFKA_CRUISE_CONTROL_SERVLET_OBJECT_CONFIG;


public abstract class AbstractRequest implements Request {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractRequest.class);
  protected KafkaCruiseControlServlet _servlet;
  protected EndPoints _endPoints;

  /**
   * Handle the request and populate the response.
   *
   * @param request Http servlet request.
   * @param response Http servlet response.
   */
  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response)
          throws Exception {
    if (parameters().parseParameters(response)) {
      LOG.warn("Failed to parse parameters: {} for request: {}.", request.getParameterMap(), request.getPathInfo());
      return;
    }

    CruiseControlResponse ccResponse = getResponse(request, response);
    ccResponse.writeSuccessResponse(parameters(), response);
  }

  @Override
  public void handle(RoutingContext context)
          throws Exception {
    if (parameters().parseParameters(context)) {
      LOG.warn("Failed to parse parameters: {} for request: {}.", context.request().params(), context.request().path());
      return;
    }

    CruiseControlResponse ccResponse = getResponse(context);
    ccResponse.writeSuccessResponse(parameters(), context);
  }

  /**
   * Get the response of the request
   * <ul>
   *   <li>Asynchronous requests return either the final response or the progress of the async request.</li>
   *   <li>Synchronous requests return the final response of the sync request.</li>
   * </ul>
   *
   * @param request Http servlet request.
   * @param response Http servlet response.
   * @return Response of the requests.
   */
  protected abstract CruiseControlResponse getResponse(HttpServletRequest request, HttpServletResponse response)
          throws Exception;

  protected abstract CruiseControlResponse getResponse(RoutingContext context)
          throws Exception;

  public abstract CruiseControlParameters parameters();

  @Override
  public void configure(Map<String, ?> configs) {
    if (configs.get(KAFKA_CRUISE_CONTROL_SERVLET_OBJECT_CONFIG).getClass().equals(KafkaCruiseControlServlet.class)) {
      _servlet = (KafkaCruiseControlServlet) validateNotNull(configs.get(KAFKA_CRUISE_CONTROL_SERVLET_OBJECT_CONFIG),
              "Kafka Cruise Control vertx configuration is missing from the request.");
    }
    if (configs.get(KAFKA_CRUISE_CONTROL_SERVLET_OBJECT_CONFIG).getClass().equals(EndPoints.class)) {
      _endPoints = (EndPoints) validateNotNull(configs.get(KAFKA_CRUISE_CONTROL_SERVLET_OBJECT_CONFIG),
              "Kafka Cruise Control vertx configuration is missing from the request.");
    }
  }
}
