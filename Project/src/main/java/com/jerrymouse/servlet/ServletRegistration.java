package com.jerrymouse.servlet;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;

public class ServletRegistration {
    private String name;
    private String className;
    private String urlPattern;
    private Servlet servlet;
    private ServletContext servletContext;
    
    public ServletRegistration(String name, String className, String urlPattern, ServletContext servletContext) {
        this.name = name;
        this.className = className;
        this.urlPattern = urlPattern;
        this.servletContext = servletContext;
    }
    
    public void load() throws ServletException {
        try {
            Class<?> clazz = Class.forName(className);
            servlet = (Servlet) clazz.getDeclaredConstructor().newInstance();
            servlet.init(new ServletConfigImpl(name, servletContext));
        } catch (Exception e) {
            throw new ServletException("Failed to load servlet: " + className, e);
        }
    }
    
    public String getName() {
        return name;
    }
    
    public String getClassName() {
        return className;
    }
    
    public String getUrlPattern() {
        return urlPattern;
    }
    
    public Servlet getServlet() {
        return servlet;
    }
}