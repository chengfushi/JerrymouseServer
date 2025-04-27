package com.jerrymouse.servlet;

import com.jerrymouse.http.HttpRequest;
import com.jerrymouse.http.HttpResponse;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServletContainer {
    private Map<String, ServletRegistration> servletRegistrations = new HashMap<>();
    private List<FilterRegistration> filterRegistrations = new ArrayList<>();
    private ServletContext servletContext;
    
    public void init() {
        // 初始化ServletContext
        servletContext = new ServletContextImpl();
        
        // 扫描webapps目录下的WEB-INF/web.xml
        loadWebConfig();
        
        // 实例化并初始化所有Servlet和Filter
        initServlets();
        initFilters();
    }
    
    public void service(HttpRequest request, HttpResponse response) {
        // 查找匹配的Servlet
        ServletRegistration registration = findServletRegistration(request.getPath());
        
        if (registration != null) {
            // 创建过滤器链
            FilterChain chain = new ApplicationFilterChain(registration.getServlet());
            
            // 执行过滤器链
            try {
                chain.doFilter(request, response);
            } catch (IOException | ServletException e) {
                // 处理异常
                response.sendError(500, "Internal Server Error");
            }
        } else {
            // 没有找到Servlet，返回404
            response.sendError(404, "Not Found");
        }
    }
    
    private void loadWebConfig() {
        // TODO: 实现web.xml配置文件加载
    }
    
    private void initServlets() {
        // TODO: 实现Servlet初始化
    }
    
    private void initFilters() {
        // TODO: 实现Filter初始化
    }
    
    private ServletRegistration findServletRegistration(String path) {
        // TODO: 实现Servlet匹配查找
        return null;
    }
    
    public void destroy() {
        // TODO: 实现资源清理
    }
}