package ch.repnik.quartzretry;

/**
 * Class with all constants related to the retry logic
 */
final class RetryConstants {

    private RetryConstants() {
        //No instantiation supported
    }

    static final String JOB_NAME = "RetryJob";
    static final String JOB_GROUP = "RetryJobs";
    static final String TRIGGER_GROUP = "RetryTriggers";
    static final String DATA_MAP_CLASSNAME = "classname";
    static final String DATA_MAP_PAYLOAD = "payload";
    static final String DATA_MAP_RETRY_COUNT = "retryCount";
    static final String DATA_MAP_RETRY_CONTEXT = "retryContext";

}
