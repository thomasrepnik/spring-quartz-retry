package ch.repnik.quartzretry;

public enum RetryState {
    NEW,
    RETRY,
    SUCCESS,
    FAILURE
}
