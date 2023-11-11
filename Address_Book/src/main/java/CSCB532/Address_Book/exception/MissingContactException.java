package CSCB532.Address_Book.exception;

public class MissingContactException extends RuntimeException{
    public MissingContactException(String message) {
        super(message);
    }

    public MissingContactException(String message, Throwable cause) {
        super(message, cause);
    }

    public MissingContactException(Throwable cause) {
        super(cause);
    }
}
