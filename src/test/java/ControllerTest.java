import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.voidhttp.HttpServer;
import net.voidhttp.controller.handler.HandlerType;
import net.voidhttp.controller.handler.Text;
import net.voidhttp.controller.route.Controller;
import net.voidhttp.controller.route.Get;
import net.voidhttp.controller.route.Post;
import net.voidhttp.request.HttpRequest;
import net.voidhttp.request.Method;
import net.voidhttp.response.HttpResponse;
import net.voidhttp.router.Middleware;
import net.voidhttp.util.asset.MIMEType;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ControllerTest {
    public static void main(String[] args) {
        HttpServer server = new HttpServer();

        registerController(server, new TestController());

        server.listen(80, () -> {
            System.out.println("Listening on port 80");
        });
    }

    @Controller("auth")
    public static class TestController {
        @Get("login")
        public String index(@Text String body) {
            System.out.println("body: " + body);
            return "Returned a string";
        }
    }

    @RequiredArgsConstructor
    private static class ParameterHandler {
        private final HandlerType type;
        private final Class<?> clazz;
    }

    private static <T> void registerController(HttpServer server, T handler) {
        Class<?> clazz = handler.getClass();

        Controller controller = clazz.getAnnotation(Controller.class);
        if (controller == null)
            throw new IllegalArgumentException("Handler does not annotate @Controller");

        String prefix = controller.value();
        if (!prefix.isEmpty())
            prefix = '/' + prefix;

        for (java.lang.reflect.Method method : clazz.getDeclaredMethods()) {
            Get get = method.getAnnotation(Get.class);
            if (get == null)
                continue;

            Class<?> returnType = method.getReturnType();

            String route = prefix + '/' + get.value();
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
                else
                    throw new IllegalArgumentException("Unrecognized parameter type " + type);
            }

            server.register(Method.GET, route, (request, response) -> {
                Object[] args = new Object[parameters.size()];
                for (int i = 0; i < parameters.size(); i++) {
                    ParameterHandler parameter = parameters.get(i);
                    if (parameter.type == HandlerType.TEXT) {
                        args[i] = request.body();
                    } else {
                        args[i] = null;
                    }
                }

                Object result = method.invoke(handler, args);
                if (CharSequence.class.isAssignableFrom(returnType)) {
                    response.send(String.valueOf(result), MIMEType.JSON);
                }
            });
        }
    }
}
