package net.voidhttp.controller.validator;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;

@UtilityClass
public class Validator {
    /**
     * Validate the specified data transfer object, that it passes all the tests specified by annotations.
     * @param value the data transfer object to validate
     * @throws Exception if the data transfer object does not pass the validation
     */
    public void validate(Object value) throws Exception {
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
            if (field.isAnnotationPresent(IsNumeric.class)) {
                String str = (String) field.get(value);
                // check if the string is not numeric
                if (!str.matches("-?\\d+(\\.\\d+)?"))
                    throw new IllegalArgumentException("Field " + field.getName() + " value " + str + " is not numeric");
            }

            // handle value null pointer validation
            if (field.isAnnotationPresent(IsNotNull.class)) {
                Object obj = field.get(value);
                // check if the value is null
                if (obj == null)
                    throw new IllegalArgumentException("Field " + field.getName() + " is null");
            }

            // handle string emptiness validation
            if (field.isAnnotationPresent(IsNotEmpty.class)) {
                String str = (String) field.get(value);
                // check if the string is not empty
                if (str.isEmpty())
                    throw new IllegalArgumentException("Field " + field.getName() + " is empty");
            }

            // handle email string validation
            if (field.isAnnotationPresent(IsEmail.class)) {
                String str = (String) field.get(value);
                // check if the string is not a valid email
                if (!str.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$"))
                    throw new IllegalArgumentException("Field " + field.getName() + " " + str + " is not an email");
            }

            // handle url string validation
            if (field.isAnnotationPresent(IsUrl.class)) {
                String str = (String) field.get(value);
                // check if a string is not a valid url
                if (!str.matches("^(http:\\/\\/www\\.|https:\\/\\/www\\.|http:\\/\\/|https:\\/\\/)?[a-z0-9]+([\\-\\.]{1}[a-z0-9]+)*\\.[a-z]{2,5}(:[0-9]{1,5})?(\\/.*)?$"))
                    throw new IllegalArgumentException("Field " + field.getName() + " " + str + " is not an url");
            }

            // handle alphanumeric string validation
            if (field.isAnnotationPresent(IsAlphanumeric.class)) {
                String str = (String) field.get(value);
                // check if a string is not alphanumeric
                if (!str.matches("^[a-zA-Z0-9]*$"))
                    throw new IllegalArgumentException("Field " + field.getName() + " " + str + " is not alphanumeric");
            }

            // handle alphabetic string validation
            if (field.isAnnotationPresent(IsAlphabetic.class)) {
                String str = (String) field.get(value);
                // check if a string is not alphabetic
                if (!str.matches("^[a-zA-Z]*$"))
                    throw new IllegalArgumentException("Field " + field.getName() + " " + str + " is not alphabetic");
            }

            // handle strong password string validation
            if (field.isAnnotationPresent(IsStrongPassword.class)) {
                IsStrongPassword password = field.getAnnotation(IsStrongPassword.class);
                String str = (String) field.get(value);
                // check if a string is not a strong password
                boolean tooShort = str.length() < password.minLength();
                boolean tooFewNumbers = str.replaceAll("[^0-9]", "").length() < password.minNumbers();
                boolean tooFewUppercase = str.replaceAll("[^A-Z]", "").length() < password.minUppercase();
                boolean tooFewSymbols = str.replaceAll("[a-zA-Z0-9]", "").length() < password.minSymbols();
                // check if the password is not strong enough
                if (tooShort || tooFewNumbers || tooFewUppercase || tooFewSymbols)
                    throw new IllegalArgumentException("Field " + field.getName() + " " + str + " is not a strong password");
            }

            // handle custom regex string validation
            if (field.isAnnotationPresent(Matches.class)) {
                Matches matches = field.getAnnotation(Matches.class);
                String str = (String) field.get(value);
                // check if a string does not match the regex
                if (!str.matches(matches.value()))
                    throw new IllegalArgumentException("Field " + field.getName() + " " + str + " does not match the regex " + matches.value());
            }
        }
    }
}
