/*
 * Copyright 2018 LinkedIn Corp. Licensed under the BSD 2-Clause License (the "License"). See License in the project root for license information.
 */
package com.linkedin.kafka.cruisecontrol.vertx.generator;

import io.swagger.v3.oas.models.OpenAPI;
import io.vertx.ext.web.Router;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author ckaratza
 * Exposes the OpenAPI spec as a vertx route.
 */
public final class OpenApiRoutePublisher {

    private final static Map<String, OpenAPI> GENERATED_SPECS = new HashMap<>();

    public synchronized static OpenAPI publishOpenApiSpec(Router router, String path, String title, String version, String serverUrl) {
        Optional<OpenAPI> spec = Optional.empty();
        if (GENERATED_SPECS.get(path) == null) {
            OpenAPI openAPI = OpenApiSpecGenerator.generateOpenApiSpecFromRouter(router, title, version, serverUrl);
            GENERATED_SPECS.put(path, openAPI);
            spec = Optional.of(openAPI);
        }
        if (spec.isPresent()) {
            Optional<OpenAPI> finalSpec = spec;
            return finalSpec.get();
        } else {
            return new OpenAPI();
        }
    }
}
