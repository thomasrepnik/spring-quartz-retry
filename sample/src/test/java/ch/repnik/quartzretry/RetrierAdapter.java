package ch.repnik.quartzretry;

import java.io.Serializable;

public class RetrierAdapter<P extends Serializable, R> extends AbstractRetrier<P, R> {

    @Override
    protected R process(P payload, RetryContext ctx) {
        return null;
    }

    @Override
    protected RetryInterval[] getRetryInterval() {
        return new RetryInterval[0];
    }

    @Override
    protected void onError(P payload, Exception e, RetryContext ctx) {

    }

    @Override
    protected void onSuccess(P payload, R result, RetryContext ctx) {

    }

    @Override
    protected void onFailure(P payload, Exception e, RetryContext ctx) {

    }
}
