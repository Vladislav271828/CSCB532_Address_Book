package CSCB532.Address_Book.exception;

public class DatabaseException extends RuntimeException {
    public DatabaseException(String message, Throwable cause) {
    }

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(Throwable cause) {
        super(cause);
    }
}
