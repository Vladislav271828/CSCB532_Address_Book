package CSCB532.Address_Book.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler
    //specific handle for BadRequestException
    //add more custom exceptions and catch them via the @ExceptionHandler to return a specific response. In this case a POJO for user related exceptions
    public ResponseEntity<ErrorResponse> handleException(BadRequestException exc){

        ErrorResponse error = new ErrorResponse();
        error.setMessage(exc.getMessage());
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setTimeStamp(System.currentTimeMillis());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    //specific handle for BadRequestException
    //add more custom exceptions and catch them via the @ExceptionHandler to return a specific response. In this case a POJO for user related exceptions
    public ResponseEntity<ErrorResponse> handleException(MissingContactException exc){

        ErrorResponse error = new ErrorResponse();
        error.setMessage(exc.getMessage());
        error.setStatus(HttpStatus.NOT_FOUND.value());
        error.setTimeStamp(System.currentTimeMillis());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }



    @ExceptionHandler
    //This catches all non specified exceptions and returns a response body of our choice
    public ResponseEntity<ErrorResponse> handleException(Exception exc){

        ErrorResponse error = new ErrorResponse();
        error.setMessage(exc.getMessage());
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setTimeStamp(System.currentTimeMillis());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleException(MethodArgumentNotValidException exc) {

        // Extract the field errors from the BindingResult
        List<FieldError> fieldErrors = exc.getBindingResult().getFieldErrors();

        // Build a string with all error messages separated by a period and space
        String errorMessage = fieldErrors.stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(". "));

        // If you want to include a period at the end of the final message
        if (!errorMessage.endsWith(".")) {
            errorMessage += ".";
        }

        ErrorResponse error = new ErrorResponse();
        error.setMessage(errorMessage); // Set the formatted error message
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setTimeStamp(System.currentTimeMillis());

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

}
