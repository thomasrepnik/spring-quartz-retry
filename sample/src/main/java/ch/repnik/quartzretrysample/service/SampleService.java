package ch.repnik.quartzretrysample.service;

import ch.repnik.quartzretry.QuartzRetriable;
import ch.repnik.quartzretry.RetryContext;
import ch.repnik.quartzretry.RetryTimeout;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;

import static ch.repnik.quartzretry.RetryTimeout.timeout;
import static org.quartz.DateBuilder.IntervalUnit.SECOND;

@Component
public class SampleService implements QuartzRetriable<Payload> {

    /*@PostConstruct
    public void init(){
        System.out.println("Sample Service created");
    }*/

    @Override
    public void onSuccess(RetryContext context) {
        System.out.println("success!!");
    }

    @Override
    public void onError(RetryContext context, Exception e) {
        System.out.println("error!! " + context.getRetryCount());
    }

    @Override
    public void onFailure(RetryContext context, Exception e) {
        System.out.println("failure!! " + context.getRetryCount());
    }

    @Override
    public RetryTimeout[] getRetryTimeouts() {
        return new RetryTimeout[] {
                timeout(3, SECOND),
                timeout(10, SECOND),
                timeout(5, SECOND)
        };
    }

    public void processAllEntities() {

        for (int i = 0; i < 20; i++) {
            Payload payload = new Payload();
        }


    }


    @Override
    public Payload selectById(String id) {
        return null;
    }

    @Override
    public void execute(Payload payload, String id) {
        System.out.println("hello from execute()");
        throw new RuntimeException("oooops");
    }


}
