package CSCB532.Address_Book.util;

import CSCB532.Address_Book.contact.DtoContact;
import CSCB532.Address_Book.exception.BadRequestException;

import java.lang.reflect.Field;

public class DtoValidationUtil {

    //return false if any fields inside the request are empty
    public static boolean checkUserDto(Object obj) { //uses reflection
        // Ensure the object is not null
        if (obj == null) {
            return false;
        }

        // Get the class of the object and its fields
        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();

        boolean isValid = true;

        // Iterate over fields
        for (Field field : fields) {
            // Set accessible true to inspect private fields
            field.setAccessible(true);

            try {
                if (field.get(obj) != null) {
                    isValid = field.get(obj).getClass().toString().isEmpty();
                } else {
                    throw new BadRequestException("Invalid data for " + obj.getClass().getSimpleName());
                }
            } catch (IllegalAccessException e) {
                throw new BadRequestException(e.getMessage());
            }

        }
        return isValid;
    }


    public static String errorMessage(Object obj) {
        // Ensure the object is not null
        if (obj == null) {
            System.out.println("Object is null");
            throw new BadRequestException("Invalid Request.");
        }

        // Get the class of the object and its fields
        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();


        StringBuilder errorMessage = new StringBuilder();

        // Iterate over fields
        for (Field field : fields) {
            // Set accessible true to inspect private fields
            field.setAccessible(true);

            try {
                if (field.get(obj).toString().isEmpty()) {
                    errorMessage.append(field.getName()).append(" ").append(field.get(obj).toString());
                }

            } catch (IllegalAccessException e) {
                throw new BadRequestException("Invalid Request.");
            }

        }

        return errorMessage.toString();
    }

    public static boolean areAllFieldsNull(Object object) {
        if (object == null) {
            return true;
        }

        for (Field field : object.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(object);
                if (value != null && !isPrimitiveAndUnset(field, value)) {
                    return false;
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Unable to access fields of object", e);
            }
        }
        return true;
    }

    private static boolean isPrimitiveAndUnset(Field field, Object value) {
        Class<?> type = field.getType();
        if (type.isPrimitive()) {
            if (type == int.class) {
                return (Integer) value == 0;
            }
            if (type == double.class) {
                return (Double) value == 0.0;
            }
            if (type == float.class) {
                return (Float) value == 0.0f;
            }
            if (type == long.class) {
                return (Long) value == 0L;
            }
            if (type == boolean.class) {
                return !((Boolean) value);
            }
            // Add other primitives as needed
        }
        return false;
    }

    public static boolean areAllContactDtoFieldsNull(DtoContact dtoContact) {
        return (
                dtoContact.getImportance() == null &&
                        dtoContact.getPhoneNumber() == null &&
                        dtoContact.getName() == null &&
                        dtoContact.getComment() == null &&
                        dtoContact.getAddress() == null &&
                        dtoContact.getFax() == null &&
                        dtoContact.getEmail() == null &&
                        dtoContact.getNameOfCompany() == null &&
                        dtoContact.getLastName() == null &&
                        dtoContact.getId() == null
        );
    }

}
