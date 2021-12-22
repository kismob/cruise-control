/*
 * Copyright 2017 LinkedIn Corp. Licensed under the BSD 2-Clause License (the "License"). See License in the project root for license information.
 */

package com.linkedin.kafka.cruisecontrol.servlet;

import com.codahale.metrics.MetricRegistry;
import com.linkedin.cruisecontrol.common.config.ConfigException;
import com.linkedin.cruisecontrol.servlet.handler.Request;
import com.linkedin.cruisecontrol.servlet.parameters.CruiseControlParameters;
import com.linkedin.kafka.cruisecontrol.CruiseControlEndPoints;
import com.linkedin.kafka.cruisecontrol.async.AsyncKafkaCruiseControl;
import com.linkedin.kafka.cruisecontrol.config.RequestParameterWrapper;
import com.linkedin.kafka.cruisecontrol.config.constants.WebServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.linkedin.kafka.cruisecontrol.servlet.CruiseControlEndPoint.REVIEW;
import static com.linkedin.kafka.cruisecontrol.servlet.CruiseControlEndPoint.REVIEW_BOARD;
import static com.linkedin.kafka.cruisecontrol.servlet.KafkaCruiseControlServletUtils.*;
import static com.linkedin.kafka.cruisecontrol.servlet.parameters.ParameterUtils.hasValidParameterNames;


/**
 * The servlet for Kafka Cruise Control.
 */
public class ServletRequestHandler extends HttpServlet {

  private static final Logger LOG = LoggerFactory.getLogger(ServletRequestHandler.class);
  protected final CruiseControlEndPoints _cruiseControlEndPoints;

  public ServletRequestHandler(AsyncKafkaCruiseControl asynckafkaCruiseControl, MetricRegistry dropwizardMetricRegistry) {
    _cruiseControlEndPoints = new CruiseControlEndPoints(asynckafkaCruiseControl, dropwizardMetricRegistry);
  }

  //only for tests
  public ServletRequestHandler(AsyncKafkaCruiseControl asynckafkaCruiseControl,
                               MetricRegistry dropwizardMetricRegistry, UserTaskManager userTaskManager) {
    _cruiseControlEndPoints = new CruiseControlEndPoints(asynckafkaCruiseControl, dropwizardMetricRegistry, userTaskManager);
  }

  @Override
  public void destroy() {
    super.destroy();
    _cruiseControlEndPoints.destroy();
  }

  protected void doOptions(HttpServletRequest request, HttpServletResponse response) {
    handleOptions(response, _cruiseControlEndPoints.config());
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    doGetOrPost(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    doGetOrPost(request, response);
  }

  private void doGetOrPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    ServletRequestContext handler = new ServletRequestContext(request, response);
    try {
      _cruiseControlEndPoints.asyncOperationStep().set(0);
      CruiseControlEndPoint endPoint = getValidEndpoint(handler, _cruiseControlEndPoints.config());
      if (endPoint != null) {
        _cruiseControlEndPoints.requestMeter().get(endPoint).mark();
        Map<String, Object> requestConfigOverrides = new HashMap<>();
        requestConfigOverrides.put(KAFKA_CRUISE_CONTROL_SERVLET_OBJECT_CONFIG, this);

        Map<String, Object> parameterConfigOverrides = new HashMap<>();
        parameterConfigOverrides.put(KAFKA_CRUISE_CONTROL_HTTP_SERVLET_REQUEST_OBJECT_CONFIG, request);
        parameterConfigOverrides.put(KAFKA_CRUISE_CONTROL_CONFIG_OBJECT_CONFIG, _cruiseControlEndPoints.config());

        switch (request.getMethod()) {
          case GET_METHOD:
            handleGet(request, response, endPoint, requestConfigOverrides, parameterConfigOverrides);
            break;
          case POST_METHOD:
            handlePost(request, response, endPoint, requestConfigOverrides, parameterConfigOverrides);
            break;
          default:
            throw new IllegalArgumentException("Unsupported request method: " + request.getMethod() + ".");
        }
      }
    } catch (UserRequestException ure) {
      String errorMessage = handleUserRequestException(ure, handler, _cruiseControlEndPoints.config());
      LOG.error(errorMessage, ure);
    } catch (ConfigException ce) {
      String errorMessage = handleConfigException(ce, handler, _cruiseControlEndPoints.config());
      LOG.error(errorMessage, ce);
    } catch (Exception e) {
      String errorMessage = handleException(e, handler, _cruiseControlEndPoints.config());
      LOG.error(errorMessage, e);
    } finally {
      try {
        response.getOutputStream().close();
      } catch (IOException e) {
        LOG.warn("Error closing output stream: ", e);
      }
    }
  }

  /**
   * The GET method allows users to perform actions supported by {@link CruiseControlEndPoint#getEndpoints()}.
   * @param request HTTP request received by Cruise Control.
   * @param response HTTP response of Cruise Control.
   * @param endPoint A GET endpoint of Cruise Control.
   * @param requestConfigOverrides Config overrides to be used while creating the {@link Request}.
   * @param parameterConfigOverrides Config overrides to be used while creating the {@link CruiseControlParameters}.
   */
  private void handleGet(HttpServletRequest request,
                         HttpServletResponse response,
                         CruiseControlEndPoint endPoint,
                         Map<String, Object> requestConfigOverrides,
                         Map<String, Object> parameterConfigOverrides)
          throws Exception {
    ServletRequestContext handler = new ServletRequestContext(request, response);
    // Sanity check: if the request is for REVIEW_BOARD, two step verification must be enabled.
    if (endPoint == REVIEW_BOARD && !_cruiseControlEndPoints.twoStepVerification()) {
      throw new ConfigException(String.format("Attempt to access %s endpoint without enabling '%s' config.",
                                              endPoint, WebServerConfig.TWO_STEP_VERIFICATION_ENABLED_CONFIG));
    }
    RequestParameterWrapper requestParameter = requestParameterFor(endPoint);
    CruiseControlParameters parameters = _cruiseControlEndPoints.config().getConfiguredInstance(requestParameter.parametersClass(),
                                                                       CruiseControlParameters.class,
                                                                       parameterConfigOverrides);
    if (hasValidParameterNames(handler, _cruiseControlEndPoints.config(), parameters)) {
      requestConfigOverrides.put(requestParameter.parameterObject(), parameters);
      Request ccRequest = _cruiseControlEndPoints.config().getConfiguredInstance(requestParameter.requestClass(),
              Request.class, requestConfigOverrides);
      ccRequest.handle(handler);
    }
  }

  /**
   * The POST method allows users to perform actions supported by {@link CruiseControlEndPoint#postEndpoints()}.
   * @param request HTTP request received by Cruise Control.
   * @param response HTTP response of Cruise Control.
   * @param endPoint A POST endpoint of Cruise Control.
   * @param requestConfigOverrides Config overrides to be used while creating the {@link Request}.
   * @param parameterConfigOverrides Config overrides to be used while creating the {@link CruiseControlParameters}.
   */
  private void handlePost(HttpServletRequest request,
                          HttpServletResponse response,
                          CruiseControlEndPoint endPoint,
                          Map<String, Object> requestConfigOverrides,
                          Map<String, Object> parameterConfigOverrides)
          throws Exception {
    ServletRequestContext handler = new ServletRequestContext(request, response);
    CruiseControlParameters parameters;
    RequestParameterWrapper requestParameter = requestParameterFor(endPoint);
    if (endPoint == REVIEW) {
      // Sanity check: if the request is for REVIEW, two step verification must be enabled.
      if (!_cruiseControlEndPoints.twoStepVerification()) {
        throw new ConfigException(String.format("Attempt to access %s endpoint without enabling '%s' config.",
                                                endPoint, WebServerConfig.TWO_STEP_VERIFICATION_ENABLED_CONFIG));
      }

      parameters = _cruiseControlEndPoints.config().getConfiguredInstance(requestParameter.parametersClass(),
              CruiseControlParameters.class, parameterConfigOverrides);
      if (!hasValidParameterNames(handler, _cruiseControlEndPoints.config(), parameters)) {
        return;
      }
    } else if (!_cruiseControlEndPoints.twoStepVerification()) {
      // Do not add to the purgatory if the two-step verification is disabled.
      parameters = _cruiseControlEndPoints.config().getConfiguredInstance(requestParameter.parametersClass(),
              CruiseControlParameters.class, parameterConfigOverrides);
      if (!hasValidParameterNames(handler, _cruiseControlEndPoints.config(), parameters)) {
        return;
      }
    } else {
      // Add to the purgatory if the two-step verification is enabled.
      parameters = _cruiseControlEndPoints.purgatory().maybeAddToPurgatory(handler, requestParameter.parametersClass(),
              parameterConfigOverrides, _cruiseControlEndPoints.userTaskManager());
    }

    Request ccRequest = null;
    if (parameters != null) {
      requestConfigOverrides.put(requestParameter.parameterObject(), parameters);
      ccRequest = _cruiseControlEndPoints.config().getConfiguredInstance(requestParameter.requestClass(), Request.class, requestConfigOverrides);
    }

    if (ccRequest != null) {
      // ccRequest would be null if request is added to Purgatory.
      ccRequest.handle(handler);
    }
  }

  public CruiseControlEndPoints cruiseControlEndPoints() {
    return _cruiseControlEndPoints;
  }
}
