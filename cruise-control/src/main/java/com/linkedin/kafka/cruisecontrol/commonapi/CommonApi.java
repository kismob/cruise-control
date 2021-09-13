/*
 * Copyright 2017 LinkedIn Corp. Licensed under the BSD 2-Clause License (the "License"). See License in the project root for license information.
 */
package com.linkedin.kafka.cruisecontrol.commonapi;

import com.linkedin.cruisecontrol.servlet.EndPoint;
import com.linkedin.kafka.cruisecontrol.servlet.KafkaCruiseControlServletUtils;
import com.linkedin.kafka.cruisecontrol.servlet.parameters.ParameterUtils;
import io.vertx.core.MultiMap;
import io.vertx.core.http.CaseInsensitiveHeaders;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import static com.linkedin.kafka.cruisecontrol.servlet.UserTaskManager.USER_TASK_HEADER_NAME;

public class CommonApi {
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

    private String _userTaskIdString;
    private String _requestURL;
    private Map<String, String[]> _queryParamsMap;
    private String _clientIdentity;
    private EndPoint _endPoint;
    private String _method;
    private String _pathInfo;
    private HttpSession _servletSession;
    private Session _vertxSession;
    private HttpServletResponse _servletResponse;
    private HttpServerResponse _vertxResponse;
    private MultiMap _headers;

    public CommonApi(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        _userTaskIdString = httpServletRequest.getHeader(USER_TASK_HEADER_NAME);
        _requestURL = String.format("%s %s", httpServletRequest.getMethod(), httpServletRequest.getRequestURI());
        _queryParamsMap = httpServletRequest.getParameterMap();
        _clientIdentity = KafkaCruiseControlServletUtils.getClientIpAddress(httpServletRequest);
        _endPoint = ParameterUtils.endPoint(httpServletRequest);
        _method = httpServletRequest.getMethod();
        _pathInfo = httpServletRequest.getPathInfo();
        _servletSession = httpServletRequest.getSession();
        _vertxSession = null;
        _servletResponse = httpServletResponse;
        _vertxResponse = null;
        _headers = getServletHeaders(httpServletRequest);
    }

    public CommonApi(RoutingContext context) {
        _userTaskIdString = context.request().getHeader(USER_TASK_HEADER_NAME);
        _requestURL = String.format("%s %s", context.request().method(), context.request().uri().split("\\?")[0]);
        _queryParamsMap = getVertxQueryParamsMap(context);
        _clientIdentity = getVertxClientIpAddress(context);
        _method = context.request().method().toString();
        _pathInfo = context.request().uri().split("\\?")[0];
        _servletSession = null;
        _vertxSession = context.session();
        _servletResponse = null;
        _vertxResponse = context.response();
        _headers = context.request().headers();
    }

    public HttpSession getServletSession() {
        return _servletSession;
    }

    public EndPoint getEndPoint() {
        return _endPoint;
    }

    public Session getVertxSession() {
        return _vertxSession;
    }

    public String getRequestURL() {
        return _requestURL;
    }

    public Map<String, String[]> getQueryParamsMap() {
        return _queryParamsMap;
    }

    public String getUserTaskIdString() {
        return _userTaskIdString;
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

    /**
     * Uses the proper way to get the header, depending on if it is using Vertx or Servlet.
     */
    public void setOrPutHeader(String name, String value) throws Exception {
        if (_vertxResponse == null && _servletResponse != null) {
            _servletResponse.setHeader(name, value);
            return;
        }
        if (_vertxResponse != null && _servletResponse == null) {
            _vertxResponse.putHeader(name, value);
            return;
        }
        throw new Exception("Something went wrong in CommonApi setOrPutHeader");
    }

    /**
     * Gives back the Vertx query parameters.
     * @return Map
     */
    public static Map<String, String[]> getVertxQueryParamsMap(RoutingContext context) {
        Map<String, String[]> queryParamsMap = new HashMap<>();
        for (Map.Entry<String, String> entry : context.queryParams().entries()) {
            queryParamsMap.put(entry.getKey(), new String[]{entry.getValue()});
        }
        return queryParamsMap;
    }

    /**
     * Returns the Vertx client IP address.
     * @return String
     */
    public static String getVertxClientIpAddress(RoutingContext context) {
        for (String header : HEADERS_TO_TRY) {
            String ip = context.request().getHeader(header);
            if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
                return "[" + ip + "]";
            }
        }
        return "[" + context.request().remoteAddress().host() + "]";
    }

    public String getMethod() {
        return _method;
    }

    public String getPathInfo() {
        return _pathInfo;
    }

    public String getClientIdentity() {
        return _clientIdentity;
    }

    public MultiMap getHeaders() {
        return _headers;
    }
}
