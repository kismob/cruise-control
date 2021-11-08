/*
 * Copyright 2018 LinkedIn Corp. Licensed under the BSD 2-Clause License (the "License"). See License in the project root for license information.
 */

package com.linkedin.kafka.cruisecontrol.httpframeworkhandler;

import com.google.gson.Gson;
import com.linkedin.cruisecontrol.httframeworkhandler.CruiseControlHttpSession;
import com.linkedin.cruisecontrol.httframeworkhandler.HttpFrameworkHandler;
import com.linkedin.kafka.cruisecontrol.KafkaCruiseControl;
import com.linkedin.kafka.cruisecontrol.config.KafkaCruiseControlConfig;
import com.linkedin.kafka.cruisecontrol.servlet.response.ResponseUtils;
import com.linkedin.kafka.cruisecontrol.vertx.VertxSession;
import io.vertx.core.MultiMap;
import io.vertx.ext.web.RoutingContext;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.linkedin.kafka.cruisecontrol.servlet.UserTaskManager.USER_TASK_HEADER_NAME;
import static com.linkedin.kafka.cruisecontrol.servlet.response.ResponseUtils.getJsonSchema;

public class VertxHttpFrameworkHandler implements HttpFrameworkHandler<KafkaCruiseControlConfig> {

    static final String[] HEADERS_TO_TRY = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
    };

    protected RoutingContext _context;

    private final CruiseControlHttpSession _session;

    public VertxHttpFrameworkHandler(RoutingContext context) {
        super();
        _context = context;
        _session = new VertxSession(context.session());
    }

    @Override
    public String getRequestURL() {
        return String.format("%s %s", _context.request().method(), _context.request().uri().split("\\?")[0]);
    }

    @Override
    public String getUserTaskIdString() {
        return _context.request().getHeader(USER_TASK_HEADER_NAME);
    }

    @Override
    public String getMethod() {
        return _context.request().method().toString();
    }

    @Override
    public String getPathInfo() {
        return _context.request().uri().split("\\?")[0];
    }

    @Override
    public String getClientIdentity() {
        return getVertxClientIpAddress(_context);
    }

    @Override
    public MultiMap getHeaders() {
        return _context.request().headers();
    }

    @Override
    public String getHeader(String header) {
        return _context.request().getHeader(header);
    }

    @Override
    public String getRemoteAddr() {
        return _context.request().remoteAddress().toString().split(":")[0];
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return getVertxQueryParamsMap(_context);
    }

    @Override
    public String getRequestUri() {
        return _context.request().uri();
    }

    @Override
    public String getParameter(String parameter) {
        return _context.queryParams().get(parameter);
    }

    @Override
    public void writeResponseToOutputStream(int responseCode, boolean json, boolean wantJsonSchema, String responseMessage,
                                            KafkaCruiseControlConfig config) throws IOException {
        ResponseUtils.setResponseCode(_context, responseCode, config);
        _context.response().putHeader("Cruise-Control-Version", KafkaCruiseControl.cruiseControlVersion());
        _context.response().putHeader("Cruise-Control-Commit_Id", KafkaCruiseControl.cruiseControlCommitId());
        if (json && wantJsonSchema) {
            _context.response().putHeader("Cruise-Control-JSON-Schema", getJsonSchema(responseMessage));
        }
        _context.response()
                .end(responseMessage);
    }

    @Override
    public CruiseControlHttpSession getSession() {
        return _session;
    }

    @Override
    public String getRequestURI() {
        return _context.request().uri().split("\\?")[0];
    }

    @Override
    public Map<String, Object> getJson() {
        Gson gson = new Gson();
        return gson.fromJson(_context.getBodyAsString(), Map.class);
    }

    @Override
    public void setHeader(String key, String value) {
        _context.response().putHeader(key, value);
    }

    /**
     * Makes a Map from the query parameters of RoutingContext.
     *
     * @param context The routing context
     * @return Returns the query parameter map
     */
    public static Map<String, String[]> getVertxQueryParamsMap(RoutingContext context) {
        Map<String, String[]> queryParamsMap = new HashMap<>();
        for (Map.Entry<String, String> entry : context.queryParams().entries()) {
            queryParamsMap.put(entry.getKey(), new String[]{entry.getValue()});
        }
        return queryParamsMap;
    }

    private String getVertxClientIpAddress(RoutingContext context) {
        for (String header : HEADERS_TO_TRY) {
            String ip = context.request().getHeader(header);
            if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
                return "[" + ip + "]";
            }
        }
        return "[" + context.request().remoteAddress().host() + "]";
    }
}
