package com.linkedin.kafka.cruisecontrol.common_api;

import com.linkedin.cruisecontrol.servlet.EndPoint;
import com.linkedin.kafka.cruisecontrol.servlet.KafkaCruiseControlServletUtils;
import com.linkedin.kafka.cruisecontrol.servlet.parameters.ParameterUtils;
import io.vertx.ext.web.RoutingContext;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.MultiMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.util.*;

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
    private HttpSession _session;


    public CommonApi(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws UnsupportedEncodingException {
        _userTaskIdString = httpServletRequest.getHeader(USER_TASK_HEADER_NAME);
        _requestURL = String.format("%s %s", httpServletRequest.getMethod(), httpServletRequest.getRequestURI());
        _queryParamsMap = httpServletRequest.getParameterMap();
        _clientIdentity = KafkaCruiseControlServletUtils.getClientIpAddress(httpServletRequest);
        _endPoint = ParameterUtils.endPoint(httpServletRequest);
        _method = httpServletRequest.getMethod();
        _pathInfo = httpServletRequest.getPathInfo();
    }

    public CommonApi(RoutingContext context){
        _userTaskIdString = context.request().getHeader(USER_TASK_HEADER_NAME);
        _requestURL = String.format("%s %s", context.request().method(), context.request().uri().split("\\?")[0]);
        _queryParamsMap = getVertxQueryParamsMap(context);
        _clientIdentity =  getVertxClientIpAddress(context);
        _method = context.request().method().toString();
        _pathInfo = context.request().uri().split("\\?")[0];
    }

    public Map<String, String[]> getVertxQueryParamsMap(RoutingContext context){
        Map<String, String[]> queryParamsMap = new HashMap<>();
        for (Map.Entry<String, String> entry : context.queryParams().entries()){
            queryParamsMap.put(entry.getKey(), new String[]{entry.getValue()});
        }
        return queryParamsMap;
    }

    public static String getVertxClientIpAddress(RoutingContext context) {
        for (String header : HEADERS_TO_TRY) {
            String ip = context.request().getHeader(header);
            if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
                return "[" + ip + "]";
            }
        }
        return "[" + context.request().remoteAddress().host() + "]";
    }


}
