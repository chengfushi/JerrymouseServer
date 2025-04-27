package com.jerrymouse.session;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HttpSessionImpl implements HttpSession {
    private final String id;
    private final long creationTime;
    private volatile long lastAccessedTime;
    private int maxInactiveInterval = 1800; // 默认30分钟
    private Map<String, Object> attributes = new ConcurrentHashMap<>();
    private boolean isValid = true;
    
    public HttpSessionImpl(String id) {
        this.id = id;
        this.creationTime = System.currentTimeMillis();
        this.lastAccessedTime = creationTime;
    }
    
    @Override
    public long getCreationTime() {
        checkValid();
        return creationTime;
    }
    
    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public long getLastAccessedTime() {
        checkValid();
        return lastAccessedTime;
    }
    
    @Override
    public ServletContext getServletContext() {
        return null; // TODO: 实现ServletContext关联
    }
    
    @Override
    public void setMaxInactiveInterval(int interval) {
        this.maxInactiveInterval = interval;
    }
    
    @Override
    public int getMaxInactiveInterval() {
        return maxInactiveInterval;
    }
    
    @Override
    public Object getAttribute(String name) {
        checkValid();
        return attributes.get(name);
    }
    
    @Override
    public Enumeration<String> getAttributeNames() {
        checkValid();
        return new java.util.Vector<>(attributes.keySet()).elements();
    }
    
    @Override
    public void setAttribute(String name, Object value) {
        checkValid();
        if (value == null) {
            removeAttribute(name);
        } else {
            attributes.put(name, value);
        }
    }
    
    @Override
    public void removeAttribute(String name) {
        checkValid();
        attributes.remove(name);
    }
    
    @Override
    public void invalidate() {
        checkValid();
        isValid = false;
        attributes.clear();
    }
    
    @Override
    public boolean isNew() {
        checkValid();
        return lastAccessedTime == creationTime;
    }
    
    public void access() {
        lastAccessedTime = System.currentTimeMillis();
    }
    
    public boolean isExpired() {
        return maxInactiveInterval > 0 && 
               System.currentTimeMillis() - lastAccessedTime > maxInactiveInterval * 1000L;
    }
    
    private void checkValid() {
        if (!isValid) {
            throw new IllegalStateException("Session already invalidated");
        }
    }
}