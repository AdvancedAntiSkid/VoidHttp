package net.voidhttp.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.voidhttp.controller.dto.Dto;
import net.voidhttp.controller.handler.*;
import net.voidhttp.request.Request;
import net.voidhttp.request.parameter.Parameters;
import net.voidhttp.response.Response;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a parameter metadata holder that indicates, what kind of data should
 * be passed to a specific parameter of a controller method.
 */
@RequiredArgsConstructor
@Getter
public class ParameterMeta {
    /**
     * The type of the parameter that indicates, what kind of data should be passed to it.
     */
    private final HandlerType handlerType;

    /**
     * The class type of the parameter.
     */
    private final Class<?> type;

    /**
     * Resolve the parameter metadata of the specified controller method.
     * @param method the controller method to resolve the parameter metadata of
     *               the method parameters
     * @return the list of the parameter metadata of the controller method
     */
    public static List<ParameterMeta> resolve(Method method) {
        List<ParameterMeta> parameters = new ArrayList<>();

        // extract parameter metadata of the class method
        Class<?>[] parameterTypes = method.getParameterTypes();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();

        // loop through the parameters of the controller method
        for (int i = 0; i < method.getParameterCount(); i++) {
            // get the type and the annotations of the parameter
            Class<?> type = method.getParameterTypes()[i];
            Annotation[] annotations = parameterAnnotations[i];

            // validate that the method parameter has only one annotation specified
            if (annotations.length != 1)
                throw new IllegalArgumentException("Handler parameter must be annotated with exactly one annotation");

            Annotation annotation = annotations[0];

            // handle parameter for raw request body
            if (annotation.annotationType() == Text.class) {
                // validate that the parameter is a valid text type
                if (!CharSequence.class.isAssignableFrom(type))
                    throw new IllegalArgumentException("Handler annotated with @Text must return a CharSequence");
                // register the parameter as a text metadata
                parameters.add(new ParameterMeta(HandlerType.TEXT, type));
            }

            // handle parameter for parsed json request body
            else if (annotation.annotationType() == Json.class) {
                // the array of types that are accepted as a json parameter
                Class<?>[] types = new Class[] {
                    CharSequence.class, JsonObject.class, JsonArray.class,
                    JsonPrimitive.class, JsonElement.class
                };

                // validate that the parameter is a valid json type
                boolean valid = false;
                for (Class<?> test : types) {
                    if (test.isAssignableFrom(type)) {
                        valid = true;
                        break;
                    }
                }
                // throw an exception if the parameter is not a valid json type
                if (!valid)
                    throw new IllegalArgumentException("Handler annotated with @Json must be a valid JSON type");

                // register the parameter as a json metadata
                parameters.add(new ParameterMeta(HandlerType.JSON, type));
            }

            // handle parameter for the request context
            else if (annotation.annotationType() == Req.class) {
                // validate that the parameter is a valid request type
                if (!Request.class.isAssignableFrom(type))
                    throw new IllegalArgumentException("Handler annotated with @Req must be a HttpRequest");
                // register the parameter as a request metadata
                parameters.add(new ParameterMeta(HandlerType.REQUEST, type));
            }

            // handle parameter for the response context
            else if (annotation.annotationType() == Res.class) {
                // validate that the parameter is a valid response type
                if (!Response.class.isAssignableFrom(type))
                    throw new IllegalArgumentException("Handler annotated with @Res must be a HttpResponse");
                // register the parameter as a response metadata
                parameters.add(new ParameterMeta(HandlerType.RESPONSE, type));
            }

            // handle a parameter for parsed request body via a data transfer object
            else if (annotation.annotationType() == Body.class) {
                // validate that the parameter is a data transfer object type
                if (!type.isAnnotationPresent(Dto.class))
                    throw new IllegalArgumentException("Handler annotated with @Body must be a DTO");
                // register the parameter as a body metadata
                parameters.add(new ParameterMeta(HandlerType.BODY, type));
            }

            // handle a parameter for the request url parameters
            else if (annotation.annotationType() == Params.class) {
                // validate that the parameter is a valid parameters type
                if (!Parameters.class.isAssignableFrom(type))
                    throw new IllegalArgumentException("Handler annotated with @Params must be a Parameters");
                // register the parameter as a parameters metadata
                parameters.add(new ParameterMeta(HandlerType.PARAMS, type));
            }

            // handle a parameter for the request query parameters
            else if (annotation.annotationType() == Query.class) {
                // validate that the parameter is a valid query type
                if (!net.voidhttp.request.query.Query.class.isAssignableFrom(type))
                    throw new IllegalArgumentException("Handler annotated with @Query must be a Query");
                // register the parameter as a query metadata
                parameters.add(new ParameterMeta(HandlerType.QUERY, type));
            }

            // handle a parameter for the request headers
            else if (annotation.annotationType() == Header.class) {
                // validate that the parameter is a valid headers type
                if (net.voidhttp.header.Headers.class.isAssignableFrom(type))
                    throw new IllegalArgumentException("Handler annotated with @Headers must be a Headers");
                // register the parameter as a headers metadata
                parameters.add(new ParameterMeta(HandlerType.HEADERS, type));
            }

            // handle a parameter for the request cookies
            else if (annotation.annotationType() == Cookie.class) {
                // validate that the parameter is a valid cookies type
                if (!net.voidhttp.request.cookie.Cookies.class.isAssignableFrom(type))
                    throw new IllegalArgumentException("Handler annotated with @Cookies must be a Cookies");
                // register the parameter as a cookies metadata
                parameters.add(new ParameterMeta(HandlerType.COOKIES, type));
            }

            // handle a parameter for the request session
            else if (annotation.annotationType() == State.class) {
                // validate that the parameter is a valid session type
                if (!net.voidhttp.request.session.Session.class.isAssignableFrom(type))
                    throw new IllegalArgumentException("Handler annotated with @Session must be a Session");
                // register the parameter as a session metadata
                parameters.add(new ParameterMeta(HandlerType.SESSION, type));
            }

            // handle a parameter for the request data
            else if (annotation.annotationType() == Data.class) {
                // validate that the parameter is a valid data type
                if (!net.voidhttp.request.data.Data.class.isAssignableFrom(type))
                    throw new IllegalArgumentException("Handler annotated with @Data must be a Data");
                // register the parameter as a data metadata
                parameters.add(new ParameterMeta(HandlerType.DATA, type));
            }

            // handle invalid parameter annotation type
            else
                throw new IllegalArgumentException("Unrecognized parameter type " + type);
        }

        return parameters;
    }
}
