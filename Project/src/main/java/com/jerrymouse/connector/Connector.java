package com.jerrymouse.connector;

import com.jerrymouse.servlet.ServletContainer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Connector implements Runnable {
    private ServerSocket serverSocket;
    private boolean running;
    private ServletContainer servletContainer;
    
    public void start() {
        try {
            serverSocket = new ServerSocket(8080);
            running = true;
            new Thread(this).start();
            System.out.println("JerrymouseServer started on port 8080");
        } catch (IOException e) {
            throw new RuntimeException("Failed to start server", e);
        }
    }
    
    @Override
    public void run() {
        while (running) {
            try {
                Socket socket = serverSocket.accept();
                new HttpHandler(socket, servletContainer).handle();
            } catch (IOException e) {
                if (running) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public void stop() {
        running = false;
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}