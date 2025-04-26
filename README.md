# JerrymouseServer
以Tomcat服务器为原型，专注于实现一个支持Servlet标准的Web服务器，即实现一个迷你版的Tomcat Server

Jerrymouse Server设计目标如下：

- 支持Servlet 6的大部分功能：
    - 支持Servlet组件；
    - 支持Filter组件；
    - 支持Listener组件；
    - 支持Sesssion（仅限Cookie模式）；
    - 不支持JSP；
    - 不支持async模式与WebSocket；
- 可部署一个标准的Web App；
- 不支持同时部署多个Web App；
- 不支持热部署。
