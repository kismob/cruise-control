/*
 * Copyright 2018 LinkedIn Corp. Licensed under the BSD 2-Clause License (the "License"). See License in the project root for license information.
 */

package com.linkedin.kafka.cruisecontrol.servlet;

import com.google.gson.Gson;
import com.linkedin.cruisecontrol.httframeworkhandler.CruiseControlHttpSession;
import com.linkedin.cruisecontrol.httframeworkhandler.HttpFrameworkHandler;
import com.linkedin.kafka.cruisecontrol.KafkaCruiseControl;
import com.linkedin.kafka.cruisecontrol.config.KafkaCruiseControlConfig;
import com.linkedin.kafka.cruisecontrol.servlet.response.ResponseUtils;
import io.vertx.core.MultiMap;
import io.vertx.core.http.CaseInsensitiveHeaders;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Map;

import static com.linkedin.kafka.cruisecontrol.servlet.UserTaskManager.USER_TASK_HEADER_NAME;

public class ServletFrameworkHandler implements HttpFrameworkHandler<KafkaCruiseControlConfig> {

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

    protected HttpServletRequest _request;
    protected HttpServletResponse _response;
    private final ServletSession _servletSession;

    public ServletFrameworkHandler(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        _request = httpServletRequest;
        _response = httpServletResponse;
        _servletSession = new ServletSession(_request.getSession());
    }

    private MultiMap getServletHeaders(HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();
        MultiMap output = new CaseInsensitiveHeaders();
        while (headerNames.hasMoreElements()) {
            String header = headerNames.nextElement();
            output.add(header, request.getHeader(header));
        }
        return output;
    }

    @Override
    public String getRequestURL() {
        return String.format("%s %s", _request.getMethod(), _request.getRequestURI());
    }

    @Override
    public String getUserTaskIdString() {
        return _request.getHeader(USER_TASK_HEADER_NAME);
    }

    @Override
    public String getMethod() {
        return _request.getMethod();
    }

    @Override
    public String getPathInfo() {
        return _request.getPathInfo();
    }

    @Override
    public String getClientIdentity() {
        return getClientIpAddress(_request);
    }

    @Override
    public MultiMap getHeaders() {
        return getServletHeaders(_request);
    }

    @Override
    public String getHeader(String header) {
        return _request.getHeader(header);
    }

    @Override
    public String getRemoteAddr() {
        return _request.getRemoteAddr();
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return _request.getParameterMap();
    }

    @Override
    public String getRequestUri() {
        return _request.getRequestURI();
    }

    @Override
    public String getParameter(String parameter) {
        return _request.getParameter(parameter);
    }

    @Override
    public void writeResponseToOutputStream(int responseCode, boolean json, boolean wantJsonSchema,
                                            String responseMessage, KafkaCruiseControlConfig config) throws IOException {
        OutputStream out = _response.getOutputStream();
        ResponseUtils.setResponseCode(_response, responseCode, json, config);
        _response.addHeader("Cruise-Control-Version", KafkaCruiseControl.cruiseControlVersion());
        _response.addHeader("Cruise-Control-Commit_Id", KafkaCruiseControl.cruiseControlCommitId());
        if (json && wantJsonSchema) {
            _response.addHeader("Cruise-Control-JSON-Schema", ResponseUtils.getJsonSchema(responseMessage));
        }
        _response.setContentLength(responseMessage.length());
        out.write(responseMessage.getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    protected String getClientIpAddress(HttpServletRequest request) {
        for (String header : HEADERS_TO_TRY) {
            String ip = request.getHeader(header);
            if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
                return ip;
            }
        }
        return request.getRemoteAddr();
    }

    @Override
    public CruiseControlHttpSession getSession() {
        return _servletSession;
    }

    @Override
    public String getRequestURI() {
        return _request.getRequestURI();
    }

    @Override
    public Map<String, Object> getJson() throws IOException {
        Gson gson = new Gson();
        return gson.fromJson(_request.getReader(), Map.class);
    }

    @Override
    public void setHeader(String key, String value) {
        _response.setHeader(key, value);
    }

    public HttpServletRequest getRequest() {
        return _request;
    }

    public HttpServletResponse getResponse() {
        return _response;
    }

    public String getRemoteHost() {
        return _request.getRemoteHost();
    }
}
