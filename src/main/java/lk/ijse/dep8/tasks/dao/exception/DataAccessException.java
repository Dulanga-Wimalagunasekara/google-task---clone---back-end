package lk.ijse.dep8.tasks.dao.exception;

public class DataAccessException extends RuntimeException{
    public DataAccessException() {
        super();
    }

    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
