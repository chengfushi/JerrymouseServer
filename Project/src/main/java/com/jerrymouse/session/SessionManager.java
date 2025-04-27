package com.jerrymouse.session;

import jakarta.servlet.http.HttpSession;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    private Map<String, HttpSession> sessions = new ConcurrentHashMap<>();
    private static final String SESSION_COOKIE_NAME = "JSESSIONID";
    
    public HttpSession getSession(String sessionId, boolean create) {
        if (sessionId != null) {
            HttpSession session = sessions.get(sessionId);
            if (session != null && !((HttpSessionImpl)session).isExpired()) {
                ((HttpSessionImpl)session).access();
                return session;
            }
        }
        
        if (create) {
            String newSessionId = generateSessionId();
            HttpSession session = new HttpSessionImpl(newSessionId);
            sessions.put(newSessionId, session);
            return session;
        }
        
        return null;
    }
    
    private String generateSessionId() {
        return UUID.randomUUID().toString();
    }
    
    public void removeSession(String sessionId) {
        sessions.remove(sessionId);
    }
    
    public String getSessionCookieName() {
        return SESSION_COOKIE_NAME;
    }
}