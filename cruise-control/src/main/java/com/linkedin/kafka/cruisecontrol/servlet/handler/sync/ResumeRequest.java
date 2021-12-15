/*
 * Copyright 2019 LinkedIn Corp. Licensed under the BSD 2-Clause License (the "License"). See License in the project root for license information.
 */

package com.linkedin.kafka.cruisecontrol.servlet.handler.sync;

import com.linkedin.kafka.cruisecontrol.CruiseControlEndPoints;
import com.linkedin.kafka.cruisecontrol.KafkaCruiseControl;
import com.linkedin.kafka.cruisecontrol.servlet.parameters.PauseResumeParameters;
import com.linkedin.kafka.cruisecontrol.servlet.response.ResumeSamplingResult;
import java.util.Map;

import static com.linkedin.kafka.cruisecontrol.servlet.parameters.ParameterUtils.PAUSE_RESUME_PARAMETER_OBJECT_CONFIG;
import static com.linkedin.cruisecontrol.common.utils.Utils.validateNotNull;


public class ResumeRequest extends AbstractSyncRequest {
  protected KafkaCruiseControl _kafkaCruiseControl;
  protected PauseResumeParameters _parameters;

  public ResumeRequest() {
    super();
  }

  @Override
  protected ResumeSamplingResult handle() {
    _kafkaCruiseControl.resumeMetricSampling(_parameters.reason());
    return new ResumeSamplingResult(_kafkaCruiseControl.config());
  }

  @Override
  public PauseResumeParameters parameters() {
    return _parameters;
  }

  @Override
  public String name() {
    return ResumeRequest.class.getSimpleName();
  }

  @Override
  public void configure(Map<String, ?> configs) {
    super.configure(configs);
    CruiseControlEndPoints cruiseControlEndPoints = _servlet == null ? _vertxHandler.cruiseControlEndPoints() : _servlet.cruiseControlEndPoints();
    _kafkaCruiseControl = cruiseControlEndPoints.asyncKafkaCruiseControl();
    _parameters = (PauseResumeParameters) validateNotNull(configs.get(PAUSE_RESUME_PARAMETER_OBJECT_CONFIG),
            "Parameter configuration is missing from the request.");
  }
}
