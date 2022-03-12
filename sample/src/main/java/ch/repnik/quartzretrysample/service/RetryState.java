package ch.repnik.quartzretrysample.service;

public enum RetryState {
    NEW,
    RETRY,
    SUCCESS,
    FAILURE
}
