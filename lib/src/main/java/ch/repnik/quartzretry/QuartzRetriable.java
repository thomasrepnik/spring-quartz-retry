package ch.repnik.quartzretry;

import java.io.Serializable;

public interface QuartzRetriable<T> {

    T selectById(String id);

    void execute(T payload, String id);

    RetryTimeout[] getRetryTimeouts();

    default void onSuccess(RetryContext context) {

    }

    default void onError(RetryContext context, Exception e) {

    }

    default void onFailure(RetryContext context, Exception e){

    }

}
