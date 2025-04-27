package com.jerrymouse.connector;

import com.jerrymouse.servlet.ServletContainer;
import com.jerrymouse.protocol.HttpRequest; // 假设HttpRequest位于com.jerrymouse.protocol包下
import com.jerrymouse.protocol.HttpResponse; // 假设HttpResponse位于com.jerrymouse.protocol包下

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class HttpHandler {
    private final Socket socket;
    private final ServletContainer servletContainer;
    
    public HttpHandler(Socket socket, ServletContainer servletContainer) {
        this.socket = socket;
        this.servletContainer = servletContainer;
    }
    
    public void handle() {
        try (InputStream input = socket.getInputStream();
             OutputStream output = socket.getOutputStream()) {
            
            // 解析HTTP请求
            HttpRequest request = parseRequest(input);
            HttpResponse response = new HttpResponse(output);
            
            // 交给Servlet容器处理
            servletContainer.service(request, response);
            
            // 发送响应
            response.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private HttpRequest parseRequest(InputStream input) throws IOException {
        // 实现HTTP请求解析逻辑
        // TODO: 实现具体的HTTP请求解析
        return null;
    }
}
