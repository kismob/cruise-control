/*
 * Copyright 2019 LinkedIn Corp. Licensed under the BSD 2-Clause License (the "License"). See License in the project root for license information.
 */

package com.linkedin.kafka.cruisecontrol.servlet.handler.sync;

import com.codahale.metrics.Timer;
import com.linkedin.cruisecontrol.httframeworkhandler.HttpFrameworkHandler;
import com.linkedin.kafka.cruisecontrol.CruiseControlEndPoints;
import com.linkedin.kafka.cruisecontrol.servlet.handler.async.runnable.OperationFuture;
import com.linkedin.cruisecontrol.servlet.EndPoint;
import com.linkedin.kafka.cruisecontrol.servlet.UserTaskManager;
import com.linkedin.kafka.cruisecontrol.servlet.handler.AbstractRequest;
import com.linkedin.kafka.cruisecontrol.servlet.handler.async.AbstractAsyncRequest;
import com.linkedin.cruisecontrol.servlet.parameters.CruiseControlParameters;
import com.linkedin.cruisecontrol.servlet.response.CruiseControlResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class AbstractSyncRequest extends AbstractRequest {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractAsyncRequest.class);
  private UserTaskManager _userTaskManager;
  private Map<EndPoint, Timer> _successfulRequestExecutionTimer;

  public AbstractSyncRequest() {

  }

  /**
   * @return Handle the request and return the response.
   */
  protected abstract CruiseControlResponse handle();

  @Override
  public CruiseControlResponse getResponse(HttpFrameworkHandler handler)
          throws Exception {
    LOG.info("Processing sync request {}.", name());
    long requestExecutionStartTime = System.nanoTime();
    int step = 0;
    OperationFuture resultFuture = _userTaskManager.getOrCreateUserTask(handler, uuid -> {
      OperationFuture future = new OperationFuture(String.format("%s request", parameters().endPoint().toString()));
      future.complete(handle());
      return future;
    }, step, false, parameters()).get(step);

    CruiseControlResponse ccResponse = resultFuture.get();
    _successfulRequestExecutionTimer.get(parameters().endPoint()).update(System.nanoTime() - requestExecutionStartTime, TimeUnit.NANOSECONDS);
    return ccResponse;
  }

  @Override
  public abstract CruiseControlParameters parameters();

  public abstract String name();

  @Override
  public void configure(Map<String, ?> configs) {
    super.configure(configs);
    CruiseControlEndPoints cruiseControlEndPoints = _servlet == null ? _vertxHandler.cruiseControlEndPoints() : _servlet.cruiseControlEndPoints();
    _userTaskManager = cruiseControlEndPoints.userTaskManager();
    _successfulRequestExecutionTimer = cruiseControlEndPoints.successfulRequestExecutionTimer();
  }
}
