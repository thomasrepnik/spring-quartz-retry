package ch.repnik.quartzretrysample.service;

import ch.repnik.quartzretry.AbstractRetrier;
import ch.repnik.quartzretry.RetryContext;
import ch.repnik.quartzretry.RetryInterval;
import org.springframework.stereotype.Component;

import java.util.Date;

import static ch.repnik.quartzretry.RetryInterval.retry;
import static org.quartz.DateBuilder.IntervalUnit.*;

@Component
public class Caller extends AbstractRetrier<Payload, String> {

    @Override
    protected String process(Payload payload, RetryContext ctx) {
        System.out.println(new Date() + ": " +
                payload.getName() +
                " sending payload to remote server (Current State: " +
                payload.getState() +" " +
                ctx.getRetryCount() +
                ") " +
                "Context: " + ctx.getDataMap());

        switch(ctx.getRetryCount()){
            case 0:
                ctx.getDataMap().put("name", "Tim");
                throw new IllegalArgumentException("Remote call failed");
            case 1:
                ctx.getDataMap().put("city", "Paris");
                throw new IllegalArgumentException("Remote call failed");
            case 2:
                ctx.getDataMap().remove("city");
                throw new IllegalArgumentException("Remote call failed");
        }

        return "Remote call successful exectued " + new Date() + " Context: " + ctx.getDataMap();
    }

    @Override
    protected RetryInterval[] getRetryInterval() {
        return new RetryInterval[] {
                retry(3, SECOND),
                retry(10, SECOND),
                retry(5, SECOND)
        };
    }

    @Override
    protected void onError(Payload entity, Exception e, RetryContext ctx) {
        entity.setState(RetryState.RETRY);
    }

    @Override
    protected void onSuccess(Payload entity, String s, RetryContext ctx) {
        entity.setState(RetryState.SUCCESS);
        System.out.println(new Date() + ": " + entity.getName() + " SUCCESS: " + s);
    }

    @Override
    protected void onFailure(Payload payload, Exception e, RetryContext ctx) {
        System.out.println("All retries failed!");
        e.printStackTrace();
    }

}
