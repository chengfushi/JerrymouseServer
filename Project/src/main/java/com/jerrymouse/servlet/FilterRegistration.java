package com.jerrymouse.servlet;

import jakarta.servlet.Filter;
import jakarta.servlet.ServletException;

public class FilterRegistration {
    private String name;
    private String className;
    private String urlPattern;
    private Filter filter;
    private ServletContext servletContext;
    
    public FilterRegistration(String name, String className, String urlPattern, ServletContext servletContext) {
        this.name = name;
        this.className = className;
        this.urlPattern = urlPattern;
        this.servletContext = servletContext;
    }
    
    public void load() throws ServletException {
        try {
            Class<?> clazz = Class.forName(className);
            filter = (Filter) clazz.getDeclaredConstructor().newInstance();
            filter.init(new FilterConfigImpl(name, servletContext));
        } catch (Exception e) {
            throw new ServletException("Failed to load filter: " + className, e);
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
    
    public Filter getFilter() {
        return filter;
    }
}