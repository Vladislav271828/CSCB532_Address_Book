package CSCB532.Address_Book.exception;

public class CustomRowNotFoundException extends RuntimeException {
    public CustomRowNotFoundException(String message) {
        super(message);
    }

    public CustomRowNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public CustomRowNotFoundException(Throwable cause) {
        super(cause);
    }
}
