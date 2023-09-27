package net.voidhttp.controller.dto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents an annotation that validates that a string is a strong password.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface IsStrongPassword {
    int minLength() default 8;
    int minNumbers() default 1;
    int minUppercase() default 1;
    int minSymbols() default 0;
}
