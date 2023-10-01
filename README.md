# VoidHttp
A simple multi-thread java webserver inspired by the syntax of Express.js and NestJS.

# Examples

## Using the modern controller system

```java
import net.voidhttp.controller.route.Controller;
import net.voidhttp.controller.route.Get;

@Controller("api")
public class MyController {
    @Get("hello")
    public String sayHello() {
        return "Hello, World!";
    }
}
```

## Using Data Transfer Objects

```java
import net.voidhttp.controller.dto.Dto;
import net.voidhttp.controller.handler.Pass;
import net.voidhttp.controller.route.Controller;

@Dto
public class LoginRequest {
    public String username;
    public String password;
}

@Dto
public class LoginResponse {
    public boolean success;
    public String token;
}

@Controller("user")
public class MyController {
    @Get("login")
    public LoginResponse onLogin(@Pass LoginRequest request) {
        System.out.println(request.username + " has logged in!");
        LoginResponse response = new LoginResponse();
        response.success = true;
        response.token = "abc-123-xyz";
        return response;
    }
}
```

## Webserver setup

```java
import net.voidhttp.HttpServer;

public class ExampleWebServer {
    public static void main(String[] args) {
        HttpServer server = new HttpServer();

        /* set up routes */

        server.listen(80, () -> {
            System.out.println("Server started");
        });
    }
}
```

## Using the classic route system

### A GET route
```java
server.get("/", (req, res) -> {
    res.send("Hello, World!");
});
```

### URL parameters
```java
Parameters parameters = req.parameters();
System.out.println("value: " + parameters.get("test"));
```

### Header handling
```java
Headers headers = req.headers();
System.out.println("user: " + headers.get("User-Agent"));
```

### Static resource folders
```java
server.use(Handlers.staticFolder("/public"));
```

### Global middlewares
```java
server.use((req, res) -> {
    System.out.println("global middleware");
});
```

### Error handling
```java
server.error(404, (req, res) -> {
    res.send("Page not found :c");
});
```

### Template rendering
```java
res.render("MyTemplate", 
    new Placeholder("name", "John Doe"), 
    new Placeholder("balance", "100$"));
```

### Redirects
```java
res.redirect("https://google.com", 5);
res.redirect("https://youtube.com");
```

### Receiving json
```java
JsonObject json = req.json();
```

### Sending json
```java
res.send(new JsonBuilder()
    .set("message", "Hello, World!")
    .set("test", true)
    .build());
```

### Response status
```java
res.status(418).message("I'm a teapot");
```

### Receiving cookies
```java
Cookies cookies = res.cookies();
System.out.println("secret: " + cookies.get("session-token"));
```

### Sending cookies
```java
Cookie cookie = new Cookie("name", "value")
    .setMaxAge(10)
    .setSecure(true)
    .setDomain("example.com");
res.cookies().add(cookie);
```
