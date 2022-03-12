package ch.repnik.quartzretrysample.service;

import java.io.Serializable;
import java.util.UUID;

public class Payload implements Serializable {

    private String name = UUID.randomUUID().toString();
    private String result;
    private RetryState state = RetryState.NEW;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
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
