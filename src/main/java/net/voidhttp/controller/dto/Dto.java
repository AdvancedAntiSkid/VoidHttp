package net.voidhttp.controller.dto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents an annotation that is required for classes to be annotated with, if they should be
 * treated as Data Transfer Objects. If the type is not annotated with Dto, and is not a VoidHttp
 * type, then an error will be thrown.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Dto {
}
