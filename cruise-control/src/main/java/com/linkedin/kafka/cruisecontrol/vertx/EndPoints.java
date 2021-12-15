/*
 * Copyright 2021 LinkedIn Corp. Licensed under the BSD 2-Clause License (the "License"). See License in the project root for license information.
 */

package com.linkedin.kafka.cruisecontrol.vertx;

import com.codahale.metrics.MetricRegistry;
import com.linkedin.cruisecontrol.common.config.ConfigException;
import com.linkedin.cruisecontrol.servlet.handler.Request;
import com.linkedin.cruisecontrol.servlet.parameters.CruiseControlParameters;
import com.linkedin.kafka.cruisecontrol.CruiseControlEndPoints;
import com.linkedin.kafka.cruisecontrol.async.AsyncKafkaCruiseControl;
import com.linkedin.kafka.cruisecontrol.config.RequestParameterWrapper;
import com.linkedin.kafka.cruisecontrol.config.constants.WebServerConfig;
import com.linkedin.kafka.cruisecontrol.httpframeworkhandler.VertxHttpFrameworkHandler;
import com.linkedin.kafka.cruisecontrol.servlet.CruiseControlEndPoint;
import com.linkedin.kafka.cruisecontrol.servlet.UserRequestException;
import org.slf4j.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.linkedin.kafka.cruisecontrol.servlet.CruiseControlEndPoint.REVIEW;
import static com.linkedin.kafka.cruisecontrol.servlet.CruiseControlEndPoint.REVIEW_BOARD;
import static com.linkedin.kafka.cruisecontrol.servlet.KafkaCruiseControlServletUtils.GET_METHOD;
import static com.linkedin.kafka.cruisecontrol.servlet.KafkaCruiseControlServletUtils.KAFKA_CRUISE_CONTROL_CONFIG_OBJECT_CONFIG;
import static com.linkedin.kafka.cruisecontrol.servlet.KafkaCruiseControlServletUtils.KAFKA_CRUISE_CONTROL_HTTP_SERVLET_REQUEST_OBJECT_CONFIG;
import static com.linkedin.kafka.cruisecontrol.servlet.KafkaCruiseControlServletUtils.KAFKA_CRUISE_CONTROL_SERVLET_OBJECT_CONFIG;
import static com.linkedin.kafka.cruisecontrol.servlet.KafkaCruiseControlServletUtils.POST_METHOD;
import static com.linkedin.kafka.cruisecontrol.servlet.KafkaCruiseControlServletUtils.ROUTING_CONTEXT_OBJECT_CONFIG;
import static com.linkedin.kafka.cruisecontrol.servlet.KafkaCruiseControlServletUtils.getValidEndpoint;
import static com.linkedin.kafka.cruisecontrol.servlet.KafkaCruiseControlServletUtils.handleConfigException;
import static com.linkedin.kafka.cruisecontrol.servlet.KafkaCruiseControlServletUtils.handleException;
import static com.linkedin.kafka.cruisecontrol.servlet.KafkaCruiseControlServletUtils.handleUserRequestException;
import static com.linkedin.kafka.cruisecontrol.servlet.KafkaCruiseControlServletUtils.requestParameterFor;
import static com.linkedin.kafka.cruisecontrol.servlet.parameters.ParameterUtils.hasValidParameterNames;

public class EndPoints implements SwaggerApi {

    private static final Logger LOG = LoggerFactory.getLogger(EndPoints.class);
    protected final CruiseControlEndPoints cruiseControlEndPoints;

    public EndPoints(AsyncKafkaCruiseControl asynckafkaCruiseControl, MetricRegistry dropwizardMetricRegistry){
        cruiseControlEndPoints = new CruiseControlEndPoints(asynckafkaCruiseControl, dropwizardMetricRegistry);
    }


    @Override
    public void destroy() {
        cruiseControlEndPoints.destroy();
    }

    private void doGetOrPost(RoutingContext context) throws IOException {
        VertxHttpFrameworkHandler handler = new VertxHttpFrameworkHandler(context);
        try {
            cruiseControlEndPoints.asyncOperationStep().set(0);
            CruiseControlEndPoint endPoint = getValidEndpoint(handler, cruiseControlEndPoints.config());
            if (endPoint != null) {
                cruiseControlEndPoints.requestMeter().get(endPoint).mark();
                Map<String, Object> requestConfigOverrides = new HashMap<>();
                requestConfigOverrides.put(KAFKA_CRUISE_CONTROL_SERVLET_OBJECT_CONFIG, this);

                Map<String, Object> parameterConfigOverrides = new HashMap<>();
                parameterConfigOverrides.put(KAFKA_CRUISE_CONTROL_HTTP_SERVLET_REQUEST_OBJECT_CONFIG, context.request());
                parameterConfigOverrides.put(KAFKA_CRUISE_CONTROL_CONFIG_OBJECT_CONFIG, cruiseControlEndPoints.config());
                parameterConfigOverrides.put(ROUTING_CONTEXT_OBJECT_CONFIG, context);
                switch (context.request().method().toString()) {
                    case GET_METHOD:
                        handleGet(context, endPoint, requestConfigOverrides, parameterConfigOverrides);
                        break;
                    case POST_METHOD:
                        handlePost(context, endPoint, requestConfigOverrides, parameterConfigOverrides);
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported request method: " + context.request().method().toString() + ".");
                }
            }
        } catch (UserRequestException ure) {
            String errorMessage = handleUserRequestException(ure, handler, cruiseControlEndPoints.config());
            LOG.error(errorMessage, ure);
        } catch (ConfigException ce) {
            String errorMessage = handleConfigException(ce, handler, cruiseControlEndPoints.config());
            LOG.error(errorMessage, ce);
        } catch (Exception e) {
            String errorMessage = handleException(e, handler, cruiseControlEndPoints.config());
            LOG.error(errorMessage, e);
        }
    }

    private void handleGet(RoutingContext context,
                           CruiseControlEndPoint endPoint,
                           Map<String, Object> requestConfigOverrides,
                           Map<String, Object> parameterConfigOverrides)
            throws Exception {
        VertxHttpFrameworkHandler handler = new VertxHttpFrameworkHandler(context);
        // Sanity check: if the request is for REVIEW_BOARD, two step verification must be enabled.
        if (endPoint == REVIEW_BOARD && !cruiseControlEndPoints.twoStepVerification()) {
            throw new ConfigException(String.format("Attempt to access %s endpoint without enabling '%s' config.",
                    endPoint, WebServerConfig.TWO_STEP_VERIFICATION_ENABLED_CONFIG));
        }
        RequestParameterWrapper requestParameter = requestParameterFor(endPoint);
        CruiseControlParameters parameters = cruiseControlEndPoints.config().getConfiguredInstance(requestParameter.parametersClass(),
                CruiseControlParameters.class,
                parameterConfigOverrides);
        if (hasValidParameterNames(handler, cruiseControlEndPoints.config(), parameters)) {
            requestConfigOverrides.put(requestParameter.parameterObject(), parameters);
            Request ccRequest = cruiseControlEndPoints.config().getConfiguredInstance(requestParameter.requestClass(), Request.class, requestConfigOverrides);

            ccRequest.handle(handler);
        }
    }

    private void handlePost(RoutingContext context,
                            CruiseControlEndPoint endPoint,
                            Map<String, Object> requestConfigOverrides,
                            Map<String, Object> parameterConfigOverrides)
            throws Exception {
        VertxHttpFrameworkHandler handler = new VertxHttpFrameworkHandler(context);
        CruiseControlParameters parameters;
        RequestParameterWrapper requestParameter = requestParameterFor(endPoint);
        if (endPoint == REVIEW) {
            // Sanity check: if the request is for REVIEW, two step verification must be enabled.
            if (!cruiseControlEndPoints.twoStepVerification()) {
                throw new ConfigException(String.format("Attempt to access %s endpoint without enabling '%s' config.",
                        endPoint, WebServerConfig.TWO_STEP_VERIFICATION_ENABLED_CONFIG));
            }

            parameters = cruiseControlEndPoints.config().getConfiguredInstance(requestParameter.parametersClass(), CruiseControlParameters.class, parameterConfigOverrides);
            if (!hasValidParameterNames(handler, cruiseControlEndPoints.config(), parameters)) {
                return;
            }
        } else if (!cruiseControlEndPoints.twoStepVerification()) {
            // Do not add to the purgatory if the two-step verification is disabled.
            parameters = cruiseControlEndPoints.config().getConfiguredInstance(requestParameter.parametersClass(), CruiseControlParameters.class, parameterConfigOverrides);
            if (!hasValidParameterNames(handler, cruiseControlEndPoints.config(), parameters)) {
                return;
            }
        } else {
            // Add to the purgatory if the two-step verification is enabled.
            parameters = cruiseControlEndPoints.purgatory().maybeAddToPurgatory(handler, requestParameter.parametersClass(), parameterConfigOverrides, cruiseControlEndPoints.userTaskManager());
        }

        Request ccRequest = null;
        if (parameters != null) {
            requestConfigOverrides.put(requestParameter.parameterObject(), parameters);
            ccRequest = cruiseControlEndPoints.config().getConfiguredInstance(requestParameter.requestClass(), Request.class, requestConfigOverrides);
        }

        if (ccRequest != null) {
            // ccRequest would be null if request is added to Purgatory.
            ccRequest.handle(handler);
        }
    }


    @Override
    public void kafkaClusterState(RoutingContext context) {
        try {
            doGetOrPost(context);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void cruiseControlState(RoutingContext context) {
        try {
            doGetOrPost(context);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void load(RoutingContext context) {
        try {
            doGetOrPost(context);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void userTasks(RoutingContext context) {
        try {
            doGetOrPost(context);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void partitionLoad(RoutingContext context) {
        try {
            doGetOrPost(context);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void proposals(RoutingContext context) {
        try {
            doGetOrPost(context);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void rebalance(RoutingContext context) {
        try {
            doGetOrPost(context);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addBroker(RoutingContext context) {
        try {
            doGetOrPost(context);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeBroker(RoutingContext context) {
        try {
            doGetOrPost(context);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void fixOfflineReplicas(RoutingContext context) {
        try {
            doGetOrPost(context);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void demoteBroker(RoutingContext context) {
        try {
            doGetOrPost(context);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stopProposalExecution(RoutingContext context) {
        try {
            doGetOrPost(context);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void pauseSampling(RoutingContext context) {
        try {
            doGetOrPost(context);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void resumeSampling(RoutingContext context) {
        try {
            doGetOrPost(context);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void topicConfiguration(RoutingContext context) {
        try {
            doGetOrPost(context);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void admin(RoutingContext context) {
        try {
            doGetOrPost(context);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void rightsize(RoutingContext context) {
        try {
            doGetOrPost(context);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public CruiseControlEndPoints cruiseControlEndPoints() {
        return cruiseControlEndPoints;
    }
}
