import com.google.gson.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.voidhttp.HttpServer;
import net.voidhttp.controller.handler.*;
import net.voidhttp.controller.route.Controller;
import net.voidhttp.controller.route.Get;
import net.voidhttp.controller.route.Post;
import net.voidhttp.request.HttpRequest;
import net.voidhttp.request.Method;
import net.voidhttp.response.HttpResponse;
import net.voidhttp.router.Middleware;
import net.voidhttp.util.asset.MIMEType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ControllerTest {
    public static void main(String[] args) throws Exception {
        HttpServer server = new HttpServer();

        registerController(server, new TestController());

        server.listenAsync(80, () -> System.out.println("Listening on port 80"));

        String response = postJson("auth/login?x=100", "{\"username\":\"test\",\"password\":\"test\"}");
        System.out.println(response);
    }

    @Controller("auth")
    public static class TestController {
        @Post("login")
        public String login(@Text String body, @Req HttpRequest req) {
            System.out.println("body: " + body);
            System.out.println("x: " + req.parameters().get("x"));
            return "Returned a string";
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

                else
                    throw new IllegalArgumentException("Unrecognized parameter type " + type);
            }

            server.register(Method.POST, route, (request, response) -> {
                Object[] args = new Object[parameters.size()];

                for (int i = 0; i < parameters.size(); i++) {
                    ParameterHandler parameter = parameters.get(i);
                    if (parameter.type == HandlerType.TEXT)
                        args[i] = request.body();

                    else if (parameter.type == HandlerType.JSON)
                        // TODO validate request body that it is a valid json
                        args[i] = CharSequence.class.isAssignableFrom(parameter.clazz) ? request.body() : gson.fromJson(request.body(), parameter.clazz);

                    else if (parameter.type == HandlerType.REQUEST)
                        args[i] = request;

                    else if (parameter.type == HandlerType.RESPONSE)
                        args[i] = response;

                    else
                        args[i] = null;
                }

                Object result = method.invoke(handler, args);
                if (CharSequence.class.isAssignableFrom(returnType))
                    response.send(String.valueOf(result), MIMEType.JSON);
                else if (JsonObject.class.isAssignableFrom(returnType))
                    response.send(result.toString(), MIMEType.JSON);
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
