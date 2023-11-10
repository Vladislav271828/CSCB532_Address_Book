package CSCB532.Address_Book.util;

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
                isValid = field.get(obj).toString().isEmpty();
            } catch (IllegalAccessException e) {
                throw new BadRequestException(e.getMessage());
            }

        }
    return isValid;
    }


    public static String errorMessage(Object obj){
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
                if (field.get(obj).toString().isEmpty()){
                    errorMessage.append(field.getName()).append(" ").append(field.get(obj).toString());
                }

            } catch (IllegalAccessException e) {
                throw new BadRequestException("Invalid Request.");
            }

        }

        return errorMessage.toString();
    }



}
