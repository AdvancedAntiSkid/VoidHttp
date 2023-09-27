package net.voidhttp.controller.handler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.voidhttp.controller.dto.Dto;

/**
 * Represents an annotation that indicates, that the parameter should be resolved from the request body
 * using the specified Data Transfer Object class. The parameter type class must annotate {@link Dto}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Body {
}
