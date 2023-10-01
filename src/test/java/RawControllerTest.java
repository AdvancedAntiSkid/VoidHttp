import com.google.gson.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.voidhttp.HttpServer;
import net.voidhttp.controller.dto.Dto;
import net.voidhttp.controller.handler.*;
import net.voidhttp.controller.route.Controller;
import net.voidhttp.controller.route.Post;
import net.voidhttp.request.HttpRequest;
import net.voidhttp.request.Method;
import net.voidhttp.request.parameter.Parameters;
import net.voidhttp.response.HttpResponse;
import net.voidhttp.util.asset.MIMEType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class RawControllerTest {
    public static void main(String[] args) throws Exception {
        HttpServer server = new HttpServer();

        registerController(server, new TestController());

        server.listenAsync(80, () -> System.out.println("Listening on port 80"));

        String response = postJson("auth/login?x=100", "{\"username\":\"admin\",\"password\":\"123\"}");
        System.out.println(response);
    }

    @Dto
    @Getter
    @ToString
    public static class LoginRequest {
        private String username;
        private String password;
    }

    @Dto
    @AllArgsConstructor
    @ToString
    public static class LoginResponse {
        private boolean success;
        private String message;
    }

    @Controller("auth")
    public static class TestController {
        @Post("login")
        public LoginResponse login(@Body LoginRequest data) {
            System.out.println("login " + data);
            // System.out.println("params " + params.get("x"));
            return new LoginResponse(true, "success");
        }
    }

    @RequiredArgsConstructor
    private static class ParameterHandler {
        private final HandlerType type;
        private final Class<?> clazz;
    }

    private static final Gson gson = new Gson();

    private static <T> void registerController(HttpServer server, T handler) {
        Class<?> clazz = handler.getClass();

        Controller controller = clazz.getAnnotation(Controller.class);
        if (controller == null)
            throw new IllegalArgumentException("Handler does not annotate @Controller");

        String prefix = controller.value();
        if (!prefix.isEmpty())
            prefix = '/' + prefix;

        for (java.lang.reflect.Method method : clazz.getDeclaredMethods()) {
            Post post = method.getAnnotation(Post.class);
            if (post == null)
                continue;

            Class<?> returnType = method.getReturnType();

            String route = prefix + '/' + post.value();
            System.out.println("reg '" + route + "'");

            List<ParameterHandler> parameters = new ArrayList<>();

            for (int i = 0; i < method.getParameterCount(); i++) {
                Class<?> type = method.getParameterTypes()[i];

                Annotation[] annotations = method.getParameterAnnotations()[i];
                if (annotations.length != 1)
                    throw new IllegalArgumentException("Handler parameter must be annotated with one annotation");
                Annotation annotation = annotations[0];

                if (annotation.annotationType() == Text.class) {
                    if (!CharSequence.class.isAssignableFrom(type))
                        throw new IllegalArgumentException("Handler annotated with @Text must return a CharSequence");
                    parameters.add(new ParameterHandler(HandlerType.TEXT, type));
                }

                else if (annotation.annotationType() == Json.class) {
                    Class<?>[] types = new Class[] {
                        CharSequence.class, JsonObject.class, JsonArray.class,
                        JsonPrimitive.class, JsonElement.class
                    };

                    boolean valid = false;
                    for (Class<?> test : types) {
                        if (test.isAssignableFrom(type)) {
                            valid = true;
                            break;
                        }
                    }

                    if (!valid)
                        throw new IllegalArgumentException("Handler annotated with @Json must be a valid JSON type");
                    parameters.add(new ParameterHandler(HandlerType.JSON, type));
                }

                else if (annotation.annotationType() == Req.class) {
                    if (!HttpRequest.class.isAssignableFrom(type))
                        throw new IllegalArgumentException("Handler annotated with @Req must be a HttpRequest");
                    parameters.add(new ParameterHandler(HandlerType.REQUEST, type));
                }

                else if (annotation.annotationType() == Res.class) {
                    if (!HttpResponse.class.isAssignableFrom(type))
                        throw new IllegalArgumentException("Handler annotated with @Res must be a HttpResponse");
                    parameters.add(new ParameterHandler(HandlerType.RESPONSE, type));
                }

                else if (annotation.annotationType() == Body.class) {
                    if (!type.isAnnotationPresent(Dto.class))
                        throw new IllegalArgumentException("Handler annotated with @Body must be a DTO");
                    parameters.add(new ParameterHandler(HandlerType.BODY, type));
                }

                else if (annotation.annotationType() == Params.class) {
                    if (!Parameters.class.isAssignableFrom(type))
                        throw new IllegalArgumentException("Handler annotated with @Params must be a Parameters");
                    parameters.add(new ParameterHandler(HandlerType.PARAMS, type));
                }

                else if (annotation.annotationType() == Query.class) {
                    if (!net.voidhttp.request.query.Query.class.isAssignableFrom(type))
                        throw new IllegalArgumentException("Handler annotated with @Query must be a Query");
                    parameters.add(new ParameterHandler(HandlerType.QUERY, type));
                }

                else if (annotation.annotationType() == Header.class) {
                    if (net.voidhttp.header.Headers.class.isAssignableFrom(type))
                        throw new IllegalArgumentException("Handler annotated with @Headers must be a Headers");
                    parameters.add(new ParameterHandler(HandlerType.HEADERS, type));
                }

                else if (annotation.annotationType() == Cookie.class) {
                    if (!net.voidhttp.request.cookie.Cookies.class.isAssignableFrom(type))
                        throw new IllegalArgumentException("Handler annotated with @Cookies must be a Cookies");
                    parameters.add(new ParameterHandler(HandlerType.COOKIES, type));
                }

                else if (annotation.annotationType() == State.class) {
                    if (!net.voidhttp.request.session.Session.class.isAssignableFrom(type))
                        throw new IllegalArgumentException("Handler annotated with @Session must be a Session");
                    parameters.add(new ParameterHandler(HandlerType.SESSION, type));
                }

                else if (annotation.annotationType() == Data.class) {
                    if (!net.voidhttp.request.data.Data.class.isAssignableFrom(type))
                        throw new IllegalArgumentException("Handler annotated with @Data must be a Data");
                    parameters.add(new ParameterHandler(HandlerType.DATA, type));
                }

                else
                    throw new IllegalArgumentException("Unrecognized parameter type " + type);
            }

            server.register(Method.POST, route, (request, response) -> {
                Object[] args = new Object[parameters.size()];

                for (int i = 0; i < parameters.size(); i++) {
                    ParameterHandler parameter = parameters.get(i);
                    if (parameter.type == HandlerType.TEXT)
                        args[i] = request.body();

                    else if (parameter.type == HandlerType.JSON) {
                        gson.fromJson(request.body(), JsonElement.class);
                        args[i] = CharSequence.class.isAssignableFrom(parameter.clazz) ? request.body() : gson.fromJson(request.body(), parameter.clazz);
                    }

                    else if (parameter.type == HandlerType.REQUEST)
                        args[i] = request;

                    else if (parameter.type == HandlerType.RESPONSE)
                        args[i] = response;

                    else if (parameter.type == HandlerType.BODY)
                        args[i] = gson.fromJson(request.body(), parameter.clazz);

                    else if (parameter.type == HandlerType.PARAMS)
                        args[i] = request.parameters();

                    else if (parameter.type == HandlerType.QUERY)
                        args[i] = request.query();

                    else if (parameter.type == HandlerType.HEADERS)
                        args[i] = request.headers();

                    else if (parameter.type == HandlerType.COOKIES)
                        args[i] = request.cookies();

                    else if (parameter.type == HandlerType.SESSION)
                        args[i] = request.session();

                    else if (parameter.type == HandlerType.DATA)
                        args[i] = request.data();

                    else
                        args[i] = null;
                }

                Object result = method.invoke(handler, args);
                if (CharSequence.class.isAssignableFrom(returnType))
                    response.send(String.valueOf(result), MIMEType.JSON);

                else if (JsonObject.class.isAssignableFrom(returnType))
                    response.send(result.toString(), MIMEType.JSON);

                else if (returnType.isAnnotationPresent(Dto.class))
                    response.send(gson.toJson(result), MIMEType.JSON);

                else {
                    System.err.println("Handler must return a CharSequence, JsonObject, or DTO");
                    response.sendError(new IllegalArgumentException("Handler must return a CharSequence, JsonObject, or DTO"));
                }
            });
        }
    }

    private static String postJson(String endpoint, String data) throws Exception {
        HttpURLConnection connection = getHttpURLConnection(endpoint, data);

        try (OutputStream stream = connection.getOutputStream()) {
            stream.write(data.getBytes(StandardCharsets.UTF_8));
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        return response.toString();
    }

    private static HttpURLConnection getHttpURLConnection(String endpoint, String data) throws IOException {
        URL url = new URL("http://localhost/" + endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 ( compatible ) ");
        connection.setRequestProperty("Accept", "*/*");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Content-Length", String.valueOf(data.length()));

        connection.setDoInput(true);
        connection.setDoOutput(true);
        return connection;
    }
}
