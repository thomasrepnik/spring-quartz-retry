package ch.repnik.quartzretry;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Aspect
@Component
public class MyAspect {

    private final RetryLogic retryLogic;

    public MyAspect(RetryLogic retryLogic){
        this.retryLogic = retryLogic;
    }

    @Before("execution(* ch.repnik.quartzretry.QuartzRetriable.execute(Object, String))")
    public void beforeMethodExecution(JoinPoint joinPoint) {
        System.out.println("Before method execution");

        Object[] methodArgs = joinPoint.getArgs();

        String id = methodArgs[1] == null ? null : methodArgs[1].toString();
        if (id == null){
            throw new NullPointerException("id cannot be null");
        }
    }

    @AfterReturning("execution(* ch.repnik.quartzretry.QuartzRetriable.execute(Object, String))")
    public void afterMethodReturning(JoinPoint joinPoint) {
        System.out.println("After method execution");

        Object[] methodArgs = joinPoint.getArgs();

        String id = methodArgs[1] == null ? null : methodArgs[1].toString();


        try {
            Optional<RetryContext> retryContext = retryLogic.findRetryContext(id);
            RetryContext finalRetryContext = retryContext.orElse(new RetryContext());

            Object target = joinPoint.getTarget();
            if (target instanceof QuartzRetriable<?>){
                ((QuartzRetriable<?>) target).onSuccess(finalRetryContext);
            }
        } catch (SchedulerException e) {
            //TODO: Custom Exception
        }

    }

    @AfterThrowing(pointcut = "execution(* ch.repnik.quartzretry.QuartzRetriable.execute(Object, String))", throwing = "exception")
    public void afterMethodThrowsException(JoinPoint joinPoint, Exception exception) {

        Object[] methodArgs = joinPoint.getArgs();
        String id = methodArgs[1] == null ? null : methodArgs[1].toString();

        Object target = joinPoint.getTarget();
        QuartzRetriable<?> service = null;
        if (target instanceof QuartzRetriable<?>){
            service = ((QuartzRetriable<?>) target);
        }else{
            throw new IllegalStateException("Object " + target + " is not of type " + QuartzRetriable.class.getName());
        }

        RetryTimeout[] timeouts = service.getRetryTimeouts();


        // If all retries exceeded, call onFailure, otherwise call onError

        try {
            boolean retriesExceeded = retryLogic.reportError(id, service.getClass().getName(), timeouts);
            RetryContext retryContext = retryLogic.findRetryContext(id).orElseThrow(() -> new IllegalStateException("no retryContext available"));



            if (retriesExceeded) {
                service.onFailure(retryContext, exception);
            }else{
                service.onError(retryContext,exception);
            }

        } catch (SchedulerException e) {
            //TODO: Custom Exception
            e.printStackTrace();
        }
    }
}

