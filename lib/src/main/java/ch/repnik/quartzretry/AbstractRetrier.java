package ch.repnik.quartzretry;


import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.SerializationUtils;

import java.io.Serializable;
import java.util.UUID;

import static org.quartz.DateBuilder.futureDate;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

public abstract class AbstractRetrier<T extends Serializable, S> {

    private Scheduler scheduler;
    private int retryCount = 0;

    protected abstract S call(T t, RetryContext ctx);

    protected abstract RetryInterval[] getRetryInterval();

    protected abstract void onError(T t, Exception e, RetryContext ctx);

    protected abstract void onSuccess(T t, S s, RetryContext ctx);

    protected abstract void onFailure(T t, Exception e, RetryContext ctx);

    void setRetryCount(int retryCount){
        this.retryCount = retryCount;
    }
    
    @Autowired
    public final void setScheduler(Scheduler scheduler){
        this.scheduler = scheduler;
    }

    private void resetRetryCount(){
        this.retryCount = 0;
    }

    public void start(T t){
        start(t, new RetryContext());
    }

    public void start(T t, RetryContext ctx){

        ctx.setRetryCount(this.retryCount);

        try{
            S s = call(t, ctx);
            onSuccess(t, s, ctx);
            resetRetryCount();
        } catch (Exception e) {
            try {
                onError(t, e, ctx);
            }finally{
                persistNewTrigger(t, e, ctx);
            }
        }

    }


    private JobDetail job() {
        return JobBuilder.newJob(RetryJob.class)
                .withIdentity("QuartzJob", "QuartzJob")
                .storeDurably()
                .build();
    }

    private Trigger trigger(String triggerName, T t, RetryContext ctx) {

        JobDataMap dataMap = new JobDataMap();
        dataMap.put("payload", SerializationUtils.serialize(t));
        dataMap.put("classname", this.getClass().getName());
        dataMap.put("retryCount", this.retryCount);
        dataMap.put("retryContext", SerializationUtils.serialize(ctx));


        RetryInterval interval = getRetryInterval()[this.retryCount];

        return TriggerBuilder.newTrigger()
                .forJob("QuartzJob", "QuartzJob")
                .startAt(
                        futureDate(interval.getNumber(), interval.getUnit())
                )
                .withSchedule(simpleSchedule().withRepeatCount(0).withMisfireHandlingInstructionFireNow())
                .withIdentity(UUID.randomUUID().toString(), "retry-trigger")
                .usingJobData(dataMap)
                .build();

    }

    private void persistNewTrigger(T t, Exception e, RetryContext ctx) {

        if (this.retryCount > getRetryInterval().length - 1){
            onFailure(t, e, ctx);
            resetRetryCount();
            return;
        }

        try{
            this.scheduler.addJob(job(), true);
            this.scheduler.scheduleJob(trigger("QuartzJob", t, ctx));
        } catch (SchedulerException ex) {
            throw new QuartzRetryException("Error while scheduling new quartz trigger for retry", e);
        }
    }

}
