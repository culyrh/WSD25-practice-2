package com.example.wsdpractice.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ApiLogger implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        System.out.println("---------- 요청 정보 ----------");
        System.out.println("메서드: " + request.getMethod());
        System.out.println("URI: " + request.getRequestURI());
        System.out.println("------------------------------");

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {

        System.out.println("---------- 응답 정보 ----------");
        System.out.println("상태 코드: " + response.getStatus());
        System.out.println("------------------------------\n");
    }
}