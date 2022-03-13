package ch.repnik.quartzretry;

/**
 * Signals all exceptions related to the retry logic
 */
public class QuartzRetryException extends RuntimeException{

    public QuartzRetryException(String message, Throwable cause) {
        super(message, cause);
    }
}
