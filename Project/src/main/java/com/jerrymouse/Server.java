package com.jerrymouse;

import com.jerrymouse.connector.Connector;
import com.jerrymouse.servlet.ServletContainer;

public class Server {
    private Connector connector;
    private ServletContainer servletContainer;
    
    public void start() {
        // 初始化连接器
        connector = new Connector();
        
        // 初始化Servlet容器
        servletContainer = new ServletContainer();
        servletContainer.init();
        
        // 启动连接器
        connector.start();
        
        // 注册关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }
    
    public void stop() {
        connector.stop();
        servletContainer.destroy();
    }
}