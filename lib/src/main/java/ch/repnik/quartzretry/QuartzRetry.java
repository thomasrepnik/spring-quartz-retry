package ch.repnik.quartzretry;


import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.util.SerializationUtils;

import javax.sql.DataSource;
import java.io.Serializable;
import java.util.UUID;

import static ch.repnik.quartzretry.RetryConstants.*;
import static org.quartz.DateBuilder.futureDate;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

/**
 * Extend this class for adding quartz retry to your application
 * @param <P> Type of the payload (e.g. could be a DTO for sending to a remote service)
 * @param <R> Type of the result (e.g. the response type of the remote service)
 */
public abstract class QuartzRetry<P extends Serializable, R> {

    private Scheduler scheduler;
    private int retryCount = 0;
    private String classname;
    private boolean isRetryDisabled;
    private boolean isDatasourceBeanAvailable;
    private static final Logger LOGGER = LoggerFactory.getLogger(QuartzRetry.class);

    protected abstract R process(P payload, RetryContext ctx);

    protected abstract RetryTimeout[] getRetryTimeouts();

    protected abstract void onError(P payload, Exception e, RetryContext ctx);

    protected abstract void onSuccess(P payload, R result, RetryContext ctx);

    protected abstract void onFailure(P payload, Exception e, RetryContext ctx);

    /**
     * Internal method for setting the retry count after reading it from quartz jobDataMap.
     * @param retryCount Current retry count
     */
    void setRetryCount(int retryCount){
        this.retryCount = retryCount;
    }

    /**
     * Only for testing purposes. Don't use!
     * @param classname the classname of the bean extending QuartzRetry
     */
    void setClassname(String classname){
        this.classname = classname;
    }

    /**
     * Performs some checks based on the given application context
     * There should not be a need to call this method manually.
     * @param appCtx The current application context
     */
    @Autowired
    public final void setApplicationContext(ApplicationContext appCtx){
        isRetryDisabled = Boolean.TRUE.equals(appCtx.getEnvironment().getProperty("quartz.retry.disabled", Boolean.class));
        if (isRetryDisabled){
            LOGGER.warn("Spring Quartz Retry will not perform any retries because 'quartz.retry.disabled' property is set to 'false'");
        }

        try{
            appCtx.getBean(DataSource.class);
            isDatasourceBeanAvailable = true;
        }catch(NoSuchBeanDefinitionException e){
            isDatasourceBeanAvailable = false;
            LOGGER.warn("Spring Quartz Retry will not perform any retries because there is no DataSource available");
        }

    }

    /**
     * Sets the current quartz scheduler automatically by injection
     * There should not be a need to call this method manually.
     * @param scheduler The current quartz scheduler instance
     */
    @Autowired
    public final void setScheduler(Scheduler scheduler){
        this.scheduler = scheduler;
    }

    /**
     * Reset the retryCount to 0
     */
    private void resetRetryCount(){
        this.retryCount = 0;
    }

    /**
     * Starts the retry attempt
     * @param payload The payload object to process
     */
    public void execute(P payload){
        execute(payload, new RetryContext());
    }

    /**
     * Starts the retry atttempt
     * @param payload The payload object to process
     * @param ctx The current RetryContext
     */
    public void execute(P payload, RetryContext ctx){

        ctx.setRetryCount(this.retryCount);

        try{
            R result = process(payload, ctx);
            onSuccess(payload, result, ctx);
            resetRetryCount();
        } catch (Exception e) {

            if (isRetryDisabled || !isDatasourceBeanAvailable || this.retryCount > getRetryTimeouts().length - 1){
                onFailure(payload, e, ctx);
                resetRetryCount();
            } else {
                try {
                    onError(payload, e, ctx);
                }finally{
                    persistNewTrigger(payload, ctx);
                }
            }

        }

    }

    /**
     * Creates the quartz job used for the retries
     * @return the created quartz job
     */
    private JobDetail job() {
        return JobBuilder.newJob(RetryJob.class)
                .withIdentity(JOB_NAME, JOB_GROUP)
                .storeDurably()
                .build();
    }

    /**
     * Creates a new trigger for the next retry and fills the quartz jobDataMap.
     * @param payload the payload which was tried to process. It will be stored for the next retry.
     * @param ctx the current RetryContext
     * @return the created quartz trigger
     */
    private Trigger trigger(P payload, RetryContext ctx) {

        JobDataMap dataMap = new JobDataMap();
        dataMap.put(DATA_MAP_PAYLOAD, SerializationUtils.serialize(payload));
        dataMap.put(DATA_MAP_CLASSNAME, this.classname != null ? this.classname:this.getClass().getName());
        dataMap.put(DATA_MAP_RETRY_COUNT, this.retryCount);
        dataMap.put(DATA_MAP_RETRY_CONTEXT, SerializationUtils.serialize(ctx));


        RetryTimeout interval = getRetryTimeouts()[this.retryCount];

        return TriggerBuilder.newTrigger()
                .forJob(JOB_NAME, JOB_GROUP)
                .startAt(
                        futureDate(interval.getNumber(), interval.getUnit())
                )
                .withSchedule(simpleSchedule().withRepeatCount(0).withMisfireHandlingInstructionFireNow())
                .withIdentity(UUID.randomUUID().toString(), TRIGGER_GROUP)
                .usingJobData(dataMap)
                .build();

    }

    /**
     * Persists a new trigger to the quartz job. This Method ist schedulung the next execution.
     * @param payload the payload which should be processed with the next retry
     * @param ctx the current RetryContext
     */
    private void persistNewTrigger(P payload, RetryContext ctx) {

        try{
            this.scheduler.addJob(job(), true);
            this.scheduler.scheduleJob(trigger(payload, ctx));
        } catch (SchedulerException ex) {
            throw new QuartzRetryException("Error while scheduling new quartz trigger for retry", ex);
        }
    }

}
