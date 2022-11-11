package com.neu.assignment.controller;

import com.neu.assignment.metrics.WebappApplicationMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AuthenticationEntryPoint extends BasicAuthenticationEntryPoint {

    @Autowired
    WebappApplicationMetrics webappApplicationMetrics;
    private static String userUnauthorizedMetric = "UserUnAuthorized";

    Logger logger = LoggerFactory.getLogger(AuthenticationEntryPoint.class);
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authEx)
            throws IOException {
        String authHeader = request.getHeader("Authorization");
        logger.info("AuthenticationEntryPoint: Auth Header = " + authHeader);
        response.addHeader("WWW-Authenticate", "Basic realm=" + getRealmName());
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        PrintWriter writer = response.getWriter();
        webappApplicationMetrics.addCount(userUnauthorizedMetric);
        writer.println("HTTP Status 401 - " + authEx.getMessage());
    }

    @Override
    public void afterPropertiesSet() {
        setRealmName("MyRealm");
        super.afterPropertiesSet();
    }
}
