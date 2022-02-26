# VoidHttp
A simple java http server using the express.js syntax.

# Examples

## Webserver setup
```java
HttpServer server = new HttpServer();

/* routes */

server.listen(80, () -> {
    System.out.println("Server started");
});
```

## A GET route
```java
server.get("/", (req, res) -> {
    res.send("Hello, World!");
});
```

## URL parameters
```java
Parameters parameters = req.parameters();
System.out.println("value: " + parameters.get("test"));
```

## Header handling
```java
Headers headers = req.headers();
System.out.println("user: " + headers.get("User-Agent"));
```

## Static resource folders
```java
server.use(HttpServer.staticFolder("/public"));
```

## Global middlewares
```java
server.use((req, res) -> {
    System.out.println("global middleware");
});
```

## Error handling
```java
server.error(404, (req, res) -> {
    res.send("Page not found :c");
});
```
