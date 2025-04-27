# JerrymouseServer
## 1. 项目简介
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


## 2. 环境准备

2.1 开发环境要求
• JDK 17 或更高版本

• Maven 3.6+

• IDE (推荐 IntelliJ IDEA 或 Eclipse)


2.2 项目初始化
```bash
mvn archetype:generate -DgroupId=com.jerrymouse -DartifactId=jerrymouse-server -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false
```

## 3. 核心模块设计

3.1 项目结构
```
jerrymouse-server/
├── src/
│   ├── main/
│   │   ├── java/com/jerrymouse/
│   │   │   ├── core/            # 核心服务器实现
│   │   │   ├── connector/       # 连接器模块
│   │   │   ├── servlet/         # Servlet 容器实现
│   │   │   ├── util/            # 工具类
│   │   │   └── Main.java        # 启动类
│   │   └── resources/
│   └── test/                    # 测试代码
├── pom.xml
└── webapps/                     # Web应用部署目录
```

3.2 依赖配置 (pom.xml)
```xml
<dependencies>
    <!-- Servlet API -->
    <dependency>
        <groupId>jakarta.servlet</groupId>
        <artifactId>jakarta.servlet-api</artifactId>
        <version>6.0.0</version>
        <scope>provided</scope>
    </dependency>
    
    <!-- 其他工具依赖 -->
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-api</artifactId>
        <version>2.0.7</version>
    </dependency>
    <dependency>
        <groupId>ch.qos.logback</groupId>
        <artifactId>logback-classic</artifactId>
        <version>1.4.7</version>
    </dependency>
</dependencies>
```

## 4. 核心功能实现

4.1 服务器启动模块

```java
// Main.java
public class Main {
    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
}
```

4.2 服务器核心类

```java
// Server.java
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
```

4.3 连接器实现

```java
// Connector.java
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
```

4.4 HTTP请求处理器

```java
// HttpHandler.java
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
        // ...
    }
}
```

4.5 Servlet容器实现

```java
// ServletContainer.java
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
    
    // 其他必要方法...
}
```

## 5. Servlet 支持实现

5.1 Servlet 注册与管理

```java
// ServletRegistration.java
public class ServletRegistration {
    private String name;
    private String className;
    private String urlPattern;
    private Servlet servlet;
    
    public void load() throws ServletException {
        try {
            Class<?> clazz = Class.forName(className);
            servlet = (Servlet) clazz.getDeclaredConstructor().newInstance();
            servlet.init(new ServletConfigImpl(name, servletContext));
        } catch (Exception e) {
            throw new ServletException("Failed to load servlet: " + className, e);
        }
    }
    
    // Getter和Setter方法...
}
```

5.2 Filter 支持实现

```java
// FilterRegistration.java
public class FilterRegistration {
    private String name;
    private String className;
    private String urlPattern;
    private Filter filter;
    
    public void load() throws ServletException {
        try {
            Class<?> clazz = Class.forName(className);
            filter = (Filter) clazz.getDeclaredConstructor().newInstance();
            filter.init(new FilterConfigImpl(name, servletContext));
        } catch (Exception e) {
            throw new ServletException("Failed to load filter: " + className, e);
        }
    }
    
    // Getter和Setter方法...
}
```

5.3 Filter 链实现

```java
// ApplicationFilterChain.java
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
```

## 6. Session 支持实现

6.1 Session 管理器

```java
// SessionManager.java
public class SessionManager {
    private Map<String, HttpSession> sessions = new ConcurrentHashMap<>();
    private static final String SESSION_COOKIE_NAME = "JSESSIONID";
    
    public HttpSession getSession(String sessionId, boolean create) {
        if (sessionId != null) {
            HttpSession session = sessions.get(sessionId);
            if (session != null && !session.isExpired()) {
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
    
    // 其他必要方法...
}
```

6.2 Session 实现

```java
// HttpSessionImpl.java
public class HttpSessionImpl implements HttpSession {
    private final String id;
    private final long creationTime;
    private volatile long lastAccessedTime;
    private int maxInactiveInterval = 1800; // 默认30分钟
    private Map<String, Object> attributes = new ConcurrentHashMap<>();
    
    public HttpSessionImpl(String id) {
        this.id = id;
        this.creationTime = System.currentTimeMillis();
        this.lastAccessedTime = creationTime;
    }
    
    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public long getCreationTime() {
        return creationTime;
    }
    
    @Override
    public long getLastAccessedTime() {
        return lastAccessedTime;
    }
    
    public void access() {
        lastAccessedTime = System.currentTimeMillis();
    }
    
    public boolean isExpired() {
        return System.currentTimeMillis() - lastAccessedTime > maxInactiveInterval * 1000L;
    }
    
    // 其他HttpSession接口方法实现...
}
```

## 7. Web应用部署

7.1 Web应用结构

```
webapps/
└── myapp/
    ├── WEB-INF/
    │   ├── web.xml
    │   └── classes/
    │       └── com/example/MyServlet.class
    └── index.html
```

7.2 web.xml 解析

```java
// WebXmlParser.java
public class WebXmlParser {
    public WebAppConfig parse(InputStream input) throws Exception {
        WebAppConfig config = new WebAppConfig();
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(input);
        
        // 解析Servlet配置
        NodeList servletNodes = doc.getElementsByTagName("servlet");
        for (int i = 0; i < servletNodes.getLength(); i++) {
            Element servletElement = (Element) servletNodes.item(i);
            String servletName = getChildText(servletElement, "servlet-name");
            String servletClass = getChildText(servletElement, "servlet-class");
            
            // 解析url-pattern
            String servletPath = null;
            NodeList servletMappingNodes = doc.getElementsByTagName("servlet-mapping");
            for (int j = 0; j < servletMappingNodes.getLength(); j++) {
                Element mappingElement = (Element) servletMappingNodes.item(j);
                String mappingName = getChildText(mappingElement, "servlet-name");
                if (mappingName.equals(servletName)) {
                    servletPath = getChildText(mappingElement, "url-pattern");
                    break;
                }
            }
            
            if (servletPath != null) {
                config.addServlet(servletName, servletClass, servletPath);
            }
        }
        
        // 解析Filter配置...
        // 解析Listener配置...
        
        return config;
    }
    
    private String getChildText(Element element, String tagName) {
        NodeList nodes = element.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent().trim();
        }
        return null;
    }
}
```

## 8. 测试与验证

8.1 测试Servlet

```java
// TestServlet.java
@WebServlet("/test")
public class TestServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();
        out.println("<html><body>");
        out.println("<h1>Hello from JerrymouseServer!</h1>");
        out.println("</body></html>");
    }
}
```

8.2 测试Filter

```java
// LoggingFilter.java
@WebFilter("/*")
public class LoggingFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        System.out.println("Request received: " + ((HttpServletRequest) request).getRequestURI());
        chain.doFilter(request, response);
        System.out.println("Response sent");
    }
    
    // 其他Filter方法...
}
```

## 9. 项目构建与运行

9.1 构建项目
```bash
mvn clean package
```

9.2 运行服务器
```bash
java -cp target/jerrymouse-server-1.0-SNAPSHOT.jar com.jerrymouse.Main
```

9.3 部署Web应用
将你的Web应用放入`webapps`目录下，结构如下：
```
webapps/
└── myapp/
    ├── WEB-INF/
    │   ├── web.xml
    │   └── classes/ (包含编译后的Servlet类)
    └── index.html
```

## 10. 扩展与优化建议
1. 性能优化：
  • 实现连接池管理
  • 添加NIO支持
  • 实现静态资源缓存


2. 功能扩展：
   • 支持多Web应用部署
   • 添加JSP支持
   • 实现热部署功能


3. 安全性增强：
   • 添加HTTPS支持
   • 实现基本的安全约束


4. 管理界面：
   • 添加简单的管理控制台
   • 实现服务器状态监控

