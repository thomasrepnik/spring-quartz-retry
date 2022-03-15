package ch.repnik.quartzretrysample.service;

import ch.repnik.quartzretry.QuartzRetry;
import ch.repnik.quartzretry.RetryContext;
import ch.repnik.quartzretry.RetryTimeout;
import org.springframework.stereotype.Service;

import java.util.Date;

import static ch.repnik.quartzretry.RetryTimeout.timeout;
import static org.quartz.DateBuilder.IntervalUnit.*;

@Service
public class SampleService extends QuartzRetry<Payload, String> {

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
    protected RetryTimeout[] getRetryTimeouts() {
        return new RetryTimeout[] {
                timeout(3, SECOND),
                timeout(10, SECOND),
                timeout(5, SECOND)
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
