package ch.repnik.quartzretry;

import java.io.Serializable;
import java.util.UUID;

public class Entity implements Serializable {

    private String name = UUID.randomUUID().toString();
    private int retryCount = 0;
    private String result;
    private RetryState state;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }


    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }


    public RetryState getState() {
        return state;
    }

    public void setState(RetryState state) {
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
