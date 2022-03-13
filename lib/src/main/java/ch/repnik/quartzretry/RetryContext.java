package ch.repnik.quartzretry;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple context class for reading and writing metadata related to the retries
 */
public final class RetryContext implements Serializable {

    private int retryCount = 0;
    private Map<String, String> dataMap = new HashMap<>();

    /**
     * Gets the current retry count<br>
     * 0 = first try (no retry done so far)
     * n = nth retry
     * @return current retry count
     */
    public int getRetryCount() {
        return retryCount;
    }

    void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    /**
     * Gets the current dataMap, for storing metadata across retries
     * @return current dataMap
     */
    public Map<String, String> getDataMap(){
        return dataMap;
    }

}
