package ch.repnik.quartzretry;

public class QuartzRetryException extends RuntimeException{

    public QuartzRetryException(String message) {
        super(message);
    }

    public QuartzRetryException(String message, Throwable cause) {
        super(message, cause);
    }
}
