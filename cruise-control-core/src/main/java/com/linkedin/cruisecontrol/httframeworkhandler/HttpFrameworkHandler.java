/*
 * Copyright 2017 LinkedIn Corp. Licensed under the BSD 2-Clause License (the "License"). See License in the project root for license information.
 */
package com.linkedin.cruisecontrol.httframeworkhandler;

import io.vertx.core.MultiMap;
import java.io.IOException;
import java.util.Map;

public interface HttpFrameworkHandler<T> {

    String getRequestURL();

    String getUserTaskIdString();

    String getMethod();

    String getPathInfo();

    String getClientIdentity();

    MultiMap getHeaders();

    String getHeader(String header);

    String getRemoteAddr();

    Map<String, String[]> getParameterMap();

    String getRequestUri();

    String getParameter(String parameter);

    void writeResponseToOutputStream(int responseCode,
                                     boolean json,
                                     boolean wantJsonSchema,
                                     String responseMessage,
                                     T config) throws IOException;

    void invalidateSession();

    long getLastAccessed();

    Object getSession();

    String getSessionId();

    String getRequestURI();

    /**
     * Returns the body in JSON.
     * @return the body in JSON
     * @throws IOException
     */
    Map<String, Object> getJson() throws IOException;

    void setHeader(String key, String value);
}
