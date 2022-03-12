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

    protected abstract S call(T t);

    protected abstract RetryInterval[] getRetryInterval();

    protected abstract void onError(T t, Exception e);

    protected abstract void onSuccess(T t, S s);

    protected abstract void onFailure(T t, Exception e);

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

        try{
            S s = call(t);
            onSuccess(t, s);
            resetRetryCount();
        } catch (Exception e) {
            try {
                onError(t, e);
            }finally{
                persistNewTrigger(t, e);
            }
        }

    }


    private JobDetail job() {
        return JobBuilder.newJob(RetryJob.class)
                .withIdentity("QuartzJob", "QuartzJob")
                .storeDurably()
                .build();
    }

    private Trigger trigger(String triggerName, T t) {

        JobDataMap dataMap = new JobDataMap();
        dataMap.put("payload", SerializationUtils.serialize(t));
        dataMap.put("classname", this.getClass().getName());
        dataMap.put("retryCount", this.retryCount);

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

    private void persistNewTrigger(T t, Exception e) {

        if (this.retryCount > getRetryInterval().length - 1){
            onFailure(t, e);
            resetRetryCount();
            return;
        }

        try{
            this.scheduler.addJob(job(), true);
            this.scheduler.scheduleJob(trigger("QuartzJob", t));
        } catch (SchedulerException ex) {
            throw new QuartzRetryException("Error while scheduling new quartz trigger for retry", e);
        }
    }

}
