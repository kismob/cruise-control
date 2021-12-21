/*
 * Copyright 2019 LinkedIn Corp. Licensed under the BSD 2-Clause License (the "License"). See License in the project root for license information.
 */

package com.linkedin.kafka.cruisecontrol.servlet.handler;

import com.linkedin.cruisecontrol.servlet.handler.Request;
import com.linkedin.cruisecontrol.servlet.parameters.CruiseControlParameters;
import com.linkedin.cruisecontrol.servlet.response.CruiseControlResponse;
import com.linkedin.cruisecontrol.httframeworkhandler.CruiseControlRequestHandler;
import com.linkedin.kafka.cruisecontrol.servlet.ServletRequestHandler;
import com.linkedin.kafka.cruisecontrol.vertx.VertxHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

import static com.linkedin.cruisecontrol.common.utils.Utils.validateNotNull;
import static com.linkedin.kafka.cruisecontrol.servlet.KafkaCruiseControlServletUtils.KAFKA_CRUISE_CONTROL_SERVLET_OBJECT_CONFIG;


public abstract class AbstractRequest implements Request {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractRequest.class);
  protected ServletRequestHandler _servlet;
  protected VertxHandler _vertxHandler;

  /**
   * Handle the request and populate the response.
   *
   */
  @Override
  public void handle(CruiseControlRequestHandler handler)
          throws Exception {

    if (parameters().parseParameters(handler)) {
      LOG.warn("Failed to parse parameters: {} for request: {}.", handler.getParameterMap(), handler.getPathInfo());
      return;
    }

    CruiseControlResponse ccResponse = getResponse(handler);
    ccResponse.writeSuccessResponse(parameters(), handler);
  }

  /**
   * Get the response of the request
   * @param handler the request handler.
   * <ul>
   *   <li>Asynchronous requests return either the final response or the progress of the async request.</li>
   *   <li>Synchronous requests return the final response of the sync request.</li>
   * </ul>
   *
   * @return Response of the requests.
   */
  protected abstract CruiseControlResponse getResponse(CruiseControlRequestHandler handler)
          throws Exception;

  public abstract CruiseControlParameters parameters();

  @Override
  public void configure(Map<String, ?> configs) {
    if (configs.get(KAFKA_CRUISE_CONTROL_SERVLET_OBJECT_CONFIG).getClass().equals(ServletRequestHandler.class)) {
      _servlet = (ServletRequestHandler) validateNotNull(configs.get(KAFKA_CRUISE_CONTROL_SERVLET_OBJECT_CONFIG),
              "Kafka Cruise Control vertx configuration is missing from the request.");
    }
    if (configs.get(KAFKA_CRUISE_CONTROL_SERVLET_OBJECT_CONFIG).getClass().equals(VertxHandler.class)) {
      _vertxHandler = (VertxHandler) validateNotNull(configs.get(KAFKA_CRUISE_CONTROL_SERVLET_OBJECT_CONFIG),
              "Kafka Cruise Control vertx configuration is missing from the request.");
    }
  }
}
