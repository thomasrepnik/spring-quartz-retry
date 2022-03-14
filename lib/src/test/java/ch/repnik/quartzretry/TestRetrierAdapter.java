package ch.repnik.quartzretry;

import java.io.Serializable;

public class TestRetrierAdapter<T extends Serializable, S> extends QuartzRetry<T, S> {

    @Override
    protected S process(T t, RetryContext ctx) {
        return null;
    }

    @Override
    protected RetryInterval[] getRetryInterval() {
        return new RetryInterval[0];
    }

    @Override
    protected void onError(T t, Exception e, RetryContext ctx) {

    }

    @Override
    protected void onSuccess(T t, S s, RetryContext ctx) {

    }

    @Override
    protected void onFailure(T t, Exception e, RetryContext ctx) {

    }
}
