package CSCB532.Address_Book.exception;

import jakarta.persistence.QueryTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@ControllerAdvice
public class ApiExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);

    private ResponseEntity<ErrorResponse> buildErrorResponse(Exception exc, HttpStatus status) {
        ErrorResponse error = new ErrorResponse();
        error.setMessage(exc.getMessage());
        error.setStatus(status.value());
        error.setTimeStamp(System.currentTimeMillis());
        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException exc){
        logger.error("BadRequestException occurred: {}", exc.getMessage());
        return buildErrorResponse(exc, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ContactNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleContactNotFoundException(ContactNotFoundException exc){
        logger.error("ContactNotFoundException occurred: {}", exc.getMessage());
        return buildErrorResponse(exc, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CustomRowNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCustomRowNotFoundException(CustomRowNotFoundException exc){
        logger.error("CustomRowNotFoundException occurred: {}", exc.getMessage());
        return buildErrorResponse(exc, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException exc) {
        List<FieldError> fieldErrors = exc.getBindingResult().getFieldErrors();
        String errorMessage = fieldErrors.stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(". "));

        if (!errorMessage.endsWith(".")) {
            errorMessage += ".";
        }

        logger.error("MethodArgumentNotValidException occurred: {}", errorMessage);
        return buildErrorResponse(new Exception(errorMessage), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        logger.error("DataIntegrityViolationException occurred: {}", ex.getMessage());
        return buildErrorResponse(new Exception("A data integrity issue occurred. Please make sure your input is valid."), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(QueryTimeoutException.class)
    public ResponseEntity<ErrorResponse> handleQueryTimeoutException(QueryTimeoutException ex) {
        logger.error("QueryTimeoutException occurred: {}", ex.getMessage());
        return buildErrorResponse(new Exception("The request timed out. Please try again later."), HttpStatus.REQUEST_TIMEOUT);
    }

    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<ErrorResponse> handleDatabaseException(DatabaseException ex) {
        logger.error("DatabaseException occurred: {}", ex.getMessage());
        return buildErrorResponse(new Exception("An unexpected database error occurred. Please contact support if the problem persists."), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception exc){
        logger.error("General Exception occurred: {}", exc.getMessage());
        return buildErrorResponse(exc, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handle(NoSuchElementException exc) {

        String errorMessage = "User with that email not found.";

        logger.error("User not found: {}", errorMessage);
        return buildErrorResponse(new Exception(errorMessage), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handle(BadCredentialsException exc) {

        String errorMessage = "Bad credentials.";

        logger.error("Incorrect Password: {}", errorMessage);
        return buildErrorResponse(new Exception(errorMessage), HttpStatus.BAD_REQUEST);
    }
}
