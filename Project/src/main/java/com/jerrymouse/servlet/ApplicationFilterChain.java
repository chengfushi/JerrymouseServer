package com.jerrymouse.servlet;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import java.io.IOException;
import java.util.List;

public class ApplicationFilterChain implements FilterChain {
    private final Servlet servlet;
    private final List<Filter> filters;
    private int position = 0;
    
    public ApplicationFilterChain(Servlet servlet, List<Filter> filters) {
        this.servlet = servlet;
        this.filters = filters;
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response) 
            throws IOException, ServletException {
        if (position < filters.size()) {
            Filter filter = filters.get(position++);
            filter.doFilter(request, response, this);
        } else {
            servlet.service(request, response);
        }
    }
}