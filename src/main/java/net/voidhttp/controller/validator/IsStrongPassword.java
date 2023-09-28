package net.voidhttp.controller.validator;

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
    /**
     * Get the minimum length of the password.
     */
    int minLength() default 8;

    /**
     * Get the minimum count of numbers in the password.
     */
    int minNumbers() default 1;

    /**
     * Get the minimum count of lowercase letters in the password.
     */
    int minUppercase() default 1;

    /**
     * Get the minimum count of uppercase letters in the password.
     */
    int minSymbols() default 0;
}
