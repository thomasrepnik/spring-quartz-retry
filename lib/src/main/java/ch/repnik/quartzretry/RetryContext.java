package ch.repnik.quartzretry;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class RetryContext implements Serializable {

    private int retryCount = 0;
    private Map<String, String> dataMap = new HashMap<>();

    public int getRetryCount() {
        return retryCount;
    }

    void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public Map<String, String> getDataMap(){
        return dataMap;
    }

}
