package ch.repnik.quartzretry;

import lombok.SneakyThrows;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.SerializationUtils;

import java.io.Serializable;
import java.util.UUID;

import static org.quartz.DateBuilder.futureDate;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

public abstract class AbstractRetrier<T extends Serializable, S> {

    private Scheduler scheduler;

    protected abstract S call(T t);

    protected abstract void onError(T t, Exception e);

    protected abstract void onSuccess(T t, S s);
    
    @Autowired
    public final void setScheduler(Scheduler scheduler){
        this.scheduler = scheduler;
    }


    public void start(T t){

        try{
            S s = call(t);
            onSuccess(t, s);
        } catch (Exception e) {
            try {
                onError(t, e);
            }finally{
                persistNewTrigger(t);
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

        return TriggerBuilder.newTrigger()
                .forJob("QuartzJob", "QuartzJob")
                .startAt(
                        futureDate(7, DateBuilder.IntervalUnit.SECOND)
                )
                .withSchedule(simpleSchedule().withRepeatCount(0).withMisfireHandlingInstructionFireNow())
                .withIdentity(UUID.randomUUID().toString(), "retry-trigger")
                .usingJobData(dataMap)
                .build();
    }

    @SneakyThrows
    private void persistNewTrigger(T t) {
        Trigger trigger = trigger("QuartzJob", t);
        this.scheduler.addJob(job(), true);
        this.scheduler.scheduleJob(trigger);
    }

}
