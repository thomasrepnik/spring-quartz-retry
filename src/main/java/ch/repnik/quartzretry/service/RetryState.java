package ch.repnik.quartzretry.service;

public enum RetryState {
    NEW,
    RETRY,
    SUCCESS,
    FAILURE
}
