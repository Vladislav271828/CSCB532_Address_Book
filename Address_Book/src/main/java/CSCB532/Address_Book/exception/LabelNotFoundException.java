package CSCB532.Address_Book.exception;


public class LabelNotFoundException extends RuntimeException{
    public LabelNotFoundException(String message) {
        super(message);
    }

    public LabelNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public LabelNotFoundException(Throwable cause) {
        super(cause);
    }
}