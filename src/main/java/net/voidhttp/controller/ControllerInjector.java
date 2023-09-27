package net.voidhttp.controller;

import com.google.gson.Gson;
import lombok.SneakyThrows;
import net.voidhttp.HttpServer;
import net.voidhttp.controller.dto.*;
import net.voidhttp.controller.handler.HandlerType;
import net.voidhttp.controller.route.*;
import net.voidhttp.router.Middleware;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a manager that registers controller blueprint classes and attaches
 * them to the http server route handling pipeline.
 */
public class ControllerInjector {
    /**
     * The http method annotations that are used to register a route listener.
     * These annotations are checked for each method of the controller class.
     */
    private static final Class<?>[] METHOD_TYPES = new Class[] {
        Get.class,     Head.class,   Post.class,
        Put.class,     Delete.class, Connect.class,
        Options.class, Trace.class,  Patch.class
    };

    /**
     * The json serializer that is used to parse the request body to the specified
     */
    private static final Gson GSON = new Gson();

    /**
     * Inject all the methods of the specified controller class into the route handler pipeline.
     * @param server the http server to inject the controller into
     * @param handler the controller instance to inject
     * @param <T> the type of the controller
     */
    public <T> void inject(HttpServer server, T handler) {
        Class<?> clazz = handler.getClass();
        // check if the class is annotated with @Controller
        Controller controller = clazz.getAnnotation(Controller.class);
        if (controller == null)
            throw new IllegalArgumentException("Handler does not annotate @Controller");

        // add a leading prefix to the route path if the blueprint root prefix
        // is not the default route
        String prefix = controller.value();
        if (!prefix.isEmpty())
            prefix = '/' + prefix;

        // check the non-inherited methods of the class
        for (Method method : clazz.getDeclaredMethods()) {
            // resolve the registered http methods of the method
            Set<MethodMeta> methods = getMethods(method);
            // ignore the class method if it is not a route listener
            if (methods.isEmpty())
                continue;

            // resole the metadata of the method parameters
            List<ParameterMeta> metaList = ParameterMeta.resolve(method);

            // create a middleware hook that will invoke the listener with the
            // transformed arguments specified by their parameter annotations
            Middleware hook = createHook(method, metaList, handler);

            // register the route listener for each method
            for (MethodMeta httpMethod : methods) {
                String route = prefix + '/' + httpMethod.getValue();
                server.register(httpMethod.getMethod(), route, hook);
            }
        }
    }


    /**
     * Resolve the registered http methods of the listener that the route should register.
     * @param method the listener method
     * @return the registered http methods
     */
    @SneakyThrows
    private Set<MethodMeta> getMethods(Method method) {
        Set<MethodMeta> methods = new HashSet<>();
        // loop through the non-inherited annotations of the class method
        for (Annotation annotation : method.getDeclaredAnnotations()) {
            // check if the annotation is a http method annotation
            for (Class<?> type : METHOD_TYPES) {
                // ignore the annotation if it is not a http method annotation
                if (!annotation.annotationType().equals(type))
                    continue;

                // resolve the value of the annotation
                Method valueMethod = type.getDeclaredMethod("value");
                String invoke = (String) valueMethod.invoke(annotation);

                // resolve the http method from the annotation name
                String methodName = type.getSimpleName().toUpperCase();
                methods.add(new MethodMeta(net.voidhttp.request.Method.of(methodName), invoke));
            }
        }
        return methods;
    }

    /**
     * Validate the specified data transfer object, that it passes all the tests specified by annotations.
     * @param value the data transfer object to validate
     * @throws Exception if the data transfer object does not pass the validation
     */
    private void validateDto(Object value) throws Exception {
        // check all the fields of the data transfer object
        for (Field field : value.getClass().getDeclaredFields()) {
            // make sure that the field is accessible
            field.setAccessible(true);

            // handle string value length validation
            if (field.isAnnotationPresent(Length.class)) {
                // get the required length of the string
                Length length = field.getAnnotation(Length.class);
                String str = (String) field.get(value);
                // check if the length of the string is out of range
                if (str.length() < length.min() || str.length() > length.max())
                    throw new IllegalArgumentException("Field " + field.getName() + " length " + str.length()
                        + " is out of range [" + length.min() + ", " + length.max() + "]");
            }

            // handle numeric value validation
            else if (field.isAnnotationPresent(IsNumeric.class)) {
                String str = (String) field.get(value);
                // check if the string is not numeric
                if (!str.matches("-?\\d+(\\.\\d+)?"))
                    throw new IllegalArgumentException("Field " + field.getName() + " value " + str + " is not numeric");
            }

            // handle value null pointer validation
            else if (field.isAnnotationPresent(IsNotNull.class)) {
                Object obj = field.get(value);
                // check if the value is null
                if (obj == null)
                    throw new IllegalArgumentException("Field " + field.getName() + " is null");
            }

            // handle string emptiness validation
            else if (field.isAnnotationPresent(IsNotEmpty.class)) {
                String str = (String) field.get(value);
                // check if the string is not empty
                if (str.isEmpty())
                    throw new IllegalArgumentException("Field " + field.getName() + " is empty");
            }

            // handle email string validation
            else if (field.isAnnotationPresent(IsEmail.class)) {
                String str = (String) field.get(value);
                // check if the string is not a valid email
                if (!str.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$"))
                    throw new IllegalArgumentException("Field " + field.getName() + " " + str + " is not an email");
            }

            // handle url string validation
            else if (field.isAnnotationPresent(IsUrl.class)) {
                String str = (String) field.get(value);
                // check if a string is not a valid url
                if (!str.matches("^(http:\\/\\/www\\.|https:\\/\\/www\\.|http:\\/\\/|https:\\/\\/)?[a-z0-9]+([\\-\\.]{1}[a-z0-9]+)*\\.[a-z]{2,5}(:[0-9]{1,5})?(\\/.*)?$"))
                    throw new IllegalArgumentException("Field " + field.getName() + " " + str + " is not an url");
            }

            // handle alphanumeric string validation
            else if (field.isAnnotationPresent(IsAlphanumeric.class)) {
                String str = (String) field.get(value);
                // check if a string is not alphanumeric
                if (!str.matches("^[a-zA-Z0-9]*$"))
                    throw new IllegalArgumentException("Field " + field.getName() + " " + str + " is not alphanumeric");
            }

            // handle alphabetic string validation
            else if (field.isAnnotationPresent(IsAlphabetic.class)) {
                String str = (String) field.get(value);
                // check if a string is not alphabetic
                if (!str.matches("^[a-zA-Z]*$"))
                    throw new IllegalArgumentException("Field " + field.getName() + " " + str + " is not alphabetic");
            }
        }
    }

    /**
     * Create a middleware that invokes the specified controller method.
     * @param method the controller class method
     * @param controller the controller instance
     * @return the middleware hook
     * @param <T> the type of the controller
     */
    private <T> Middleware createHook(Method method, List<ParameterMeta> metaList, T controller) {
        return (request, response) -> {
            // create an array to hold the resolved arguments for the method
            Object[] args = new Object[metaList.size()];

            // transform the request data to the method parameters
            for (int i = 0; i < metaList.size(); i++) {
                ParameterMeta meta = metaList.get(i);
                // handle raw body text argument
                if (meta.getHandlerType() == HandlerType.TEXT)
                    args[i] = request.body();

                // handle parsed json body argument
                else if (meta.getHandlerType() == HandlerType.JSON) {
                    // validate the json syntax even if the parameter expects a CharSequence
                    // this ensures that a valid JSON is passed to @Json annotated parameters
                    Object json = GSON.fromJson(request.body(), meta.getType());
                    if (CharSequence.class.isAssignableFrom(meta.getType()))
                        args[i] = json.toString();
                    else
                        args[i] = json;
                }

                // handle request context argument
                else if (meta.getHandlerType() == HandlerType.REQUEST)
                    args[i] = request;

                    // handle response context argument
                else if (meta.getHandlerType() == HandlerType.RESPONSE)
                    args[i] = response;

                    // handle data transform object argument
                else if (meta.getHandlerType() == HandlerType.BODY) {
                    Object value = GSON.fromJson(request.body(), meta.getType());
                    try {
                        validateDto(value);
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                    }
                    args[i] = value;
                }

                    // handle request url parameters argument
                else if (meta.getHandlerType() == HandlerType.PARAMS)
                    args[i] = request.parameters();

                    // handle request query argument
                else if (meta.getHandlerType() == HandlerType.QUERY)
                    args[i] = request.query();

                    // handle request headers argument
                else if (meta.getHandlerType() == HandlerType.HEADERS)
                    args[i] = request.headers();

                    // handle request cookies argument
                else if (meta.getHandlerType() == HandlerType.COOKIES)
                    args[i] = request.cookies();

                    // handle request session argument
                else if (meta.getHandlerType() == HandlerType.SESSION)
                    args[i] = request.session();

                    // handle passed request data argument
                else if (meta.getHandlerType() == HandlerType.DATA)
                    args[i] = request.data();
            }

            // invoke the route listener method using transformed arguments
            method.invoke(controller, args);
        };
    }
}
