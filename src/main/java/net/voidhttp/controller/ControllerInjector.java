package net.voidhttp.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.inventex.octa.concurrent.future.Future;
import lombok.SneakyThrows;
import net.voidhttp.HttpServer;
import net.voidhttp.controller.dto.Dto;
import net.voidhttp.controller.guard.UseGuard;
import net.voidhttp.controller.guard.Handler;
import net.voidhttp.controller.guard.Guard;
import net.voidhttp.controller.handler.HandlerType;
import net.voidhttp.controller.route.*;
import net.voidhttp.controller.validator.*;
import net.voidhttp.router.Middleware;
import net.voidhttp.util.asset.MIMEType;
import net.voidhttp.util.console.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
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

            // resolve the registered middlewares of the method
            List<Middleware> middlewares = getMiddlewares(method);

            // create a middleware hook that will invoke the listener with the
            // transformed arguments specified by their parameter annotations
            Middleware hook = createHook(method, metaList, handler, false);

            // register the route listener for each method
            for (MethodMeta httpMethod : methods) {
                // resolve the route path from the method annotation
                String route = prefix + '/' + httpMethod.getValue();

                // add the route handler hook to the list of middlewares
                middlewares.add(hook);
                Middleware[] handlers = middlewares.toArray(new Middleware[0]);

                // register the route listener for the controller
                server.register(httpMethod.getMethod(), route, handlers);
            }
        }
    }

    /**
     * Resolve the registered middleware classes of the listener that the route should register.
     * @param method the listener method
     * @return the list of the instantiated middlewares
     */
    private List<Middleware> getMiddlewares(Method method) {
        List<Middleware> middlewares = new ArrayList<>();
        // loop through the non-inherited annotations of the class method
        for (Annotation annotation : method.getDeclaredAnnotations()) {
            // check if the annotation is not a middleware annotation
            if (!annotation.annotationType().equals(UseGuard.class))
                continue;

            // resolve the middleware class from the annotation
            UseGuard guard = (UseGuard) annotation;
            Middleware middleware;
            try {
                // instantiate the middleware class
                Class<?> clazz = guard.value();

                if (Middleware.class.isAssignableFrom(clazz)) {
                    Constructor<?> constructor = clazz.getDeclaredConstructor();
                    constructor.setAccessible(true);
                    middleware = (Middleware) constructor.newInstance();
                } else {
                    // check if the class is annotated with @Preprocess
                    Guard preprocess = clazz.getAnnotation(Guard.class);
                    if (preprocess == null)
                        throw new IllegalArgumentException("Guard does not annotate @Middleware");

                    // resolve the handler method of the middleware class
                    Method handlerMethod = null;
                    for (Method declaredMethod : clazz.getDeclaredMethods()) {
                        if (!declaredMethod.isAnnotationPresent(Handler.class))
                            continue;
                        handlerMethod = declaredMethod;
                        break;
                    }

                    // check if there is no handler method of the middleware
                    if (handlerMethod == null)
                        throw new IllegalArgumentException("Guard does have any methods annotated with @Handler");

                    // instantiate the middleware class
                    Constructor<?> constructor = clazz.getDeclaredConstructor();
                    constructor.setAccessible(true);

                    Object handler = constructor.newInstance();

                    // resole the metadata of the method parameters
                    List<ParameterMeta> metaList = ParameterMeta.resolve(handlerMethod);

                    // create a middleware hook that will invoke the middleware with the
                    // transformed arguments specified by their parameter annotations
                    middleware = createHook(handlerMethod, metaList, handler, true);
                }

            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            // add the middleware to the list
            middlewares.add(middleware);
        }
        return middlewares;
    }

    /**
     * Create a middleware that invokes the specified controller method.
     * @param method the controller class method
     * @param controller the controller instance
     * @return the middleware hook
     * @param <T> the type of the controller
     */
    private <T> Middleware createHook(Method method, List<ParameterMeta> metaList, T controller, boolean isGuard) {
        return (request, response) -> {
            // create an array to hold the resolved arguments for the method
            Object[] args = new Object[metaList.size()];

            // retrieve the return type of the method
            Class<?> returnType = method.getReturnType();

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
                        Validator.validate(value);
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
            Object result = null;
            try {
                method.setAccessible(true);
                result = method.invoke(controller, args);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // handle raw string http response
            if (CharSequence.class.isAssignableFrom(returnType))
                response.send(String.valueOf(result), MIMEType.JSON);

            // handle wrapped json http response
            else if (JsonObject.class.isAssignableFrom(returnType))
                response.send(result.toString(), MIMEType.JSON);

            // handle wrapped dto http response
            else if (returnType.isAnnotationPresent(Dto.class))
                response.send(GSON.toJson(result), MIMEType.JSON);

            // handle asynchronous http response
            else if (returnType.isAssignableFrom(Future.class)) {
                // complete the future on the request thread
                Future<?> future = (Future<?>) result;
                try {
                    Object value = future.get();

                    // handle raw string http response
                    if (CharSequence.class.isAssignableFrom(value.getClass()))
                        response.send(String.valueOf(value), MIMEType.JSON);

                    // handle wrapped json http response
                    else if (JsonObject.class.isAssignableFrom(value.getClass()))
                        response.send(value.toString(), MIMEType.JSON);

                    // handle wrapped dto http response
                    else if (value.getClass().isAnnotationPresent(Dto.class))
                        response.send(GSON.toJson(value), MIMEType.JSON);
                }
                // handle exception whilst completing the future
                catch (Exception e) {
                    e.printStackTrace();
                    response.sendError(e);
                }
            }

            // handle invalid return type
            else if (!isGuard) {
                Logger.error("Handler must return a CharSequence, JsonObject, or DTO");
                response.sendError(new IllegalArgumentException("Handler must return a CharSequence, JsonObject, or DTO"));
            }
        };
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
}
