package net.voidhttp.controller.route;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents a controller that is a more comfortable alternative to normal blueprints.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Controller {
    /**
     * Get the prefix of the controller that is put before each registered route
     * @return controller prefix <strong>without</strong> a leading slash
     */
    String value() default "";
}
