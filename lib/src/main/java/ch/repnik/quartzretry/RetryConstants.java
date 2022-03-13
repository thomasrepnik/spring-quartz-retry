package ch.repnik.quartzretry;

/**
 * Class with all constants related to the retry logic
 */
final class RetryConstants {

    private RetryConstants() {
        //No instantiation supported
    }

    static final String JOB_NAME = "QuartzJob";
    static final String JOB_GROUP = "QuartzJob";
    static final String DATA_MAP_CLASSNAME = "classname";
    static final String DATA_MAP_PAYLOAD = "payload";
    static final String DATA_MAP_RETRY_COUNT = "retryCount";
    static final String DATA_MAP_RETRY_CONTEXT = "retryContext";

}
