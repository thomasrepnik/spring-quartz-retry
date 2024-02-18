package ch.repnik.quartzretry;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static ch.repnik.quartzretry.RetryConstants.*;
import static ch.repnik.quartzretry.RetryConstants.TRIGGER_GROUP;
import static org.quartz.DateBuilder.futureDate;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

@Component
public class RetryLogic {

    private Scheduler scheduler;
    private boolean isRetryDisabled;
    private boolean isDatasourceBeanAvailable;
    private static final Logger LOGGER = LoggerFactory.getLogger(QuartzRetry.class);


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

    public boolean reportError(String id, String className, RetryTimeout[] timeouts) throws SchedulerException {
        RetryContext retryContext = findRetryContext(id).orElse(new RetryContext());

        // Check if retry timeouts exceeded
        if (retryContext.getRetryCount() > timeouts.length) {
            // Retries exceeded, no need to set new trigger
            return true;
        }

        RetryTimeout nextTimeout = timeouts[retryContext.getRetryCount()];
        retryContext.setRetryCount(retryContext.getRetryCount() + 1);
        this.persistNewTrigger(id, className, nextTimeout, retryContext);
        return false;
    }

    private JobDataMap getJobDataMap(String id) throws SchedulerException {
        Optional<Trigger> trigger = findTrigger(id);
        return trigger.map(Trigger::getJobDataMap).orElse(null);
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
     * @param id the identifier for the object trying to process
     * @param ctx the current RetryContext
     * @return the created quartz trigger
     */
    private Trigger trigger(String id, String className, RetryTimeout retryTimeout, RetryContext ctx) {

        JobDataMap dataMap = new JobDataMap();
        dataMap.put(DATA_OBJECT_ID, id);
        dataMap.put(DATA_MAP_CLASSNAME, className);
        dataMap.put(DATA_MAP_RETRY_COUNT, ctx.getRetryCount());
        dataMap.put(DATA_MAP_RETRY_CONTEXT, SerializationUtils.serialize(ctx));


        return TriggerBuilder.newTrigger()
                .forJob(JOB_NAME, JOB_GROUP)
                .startAt(
                        futureDate(retryTimeout.getNumber(), retryTimeout.getUnit())
                )
                .withSchedule(simpleSchedule().withRepeatCount(0).withMisfireHandlingInstructionFireNow())
                .withIdentity(UUID.randomUUID().toString(), TRIGGER_GROUP)
                .usingJobData(dataMap)
                .build();

    }

    /**
     * Persists a new trigger to the quartz job. This Method ist scheduling the next execution.
     *
     * @param id          the payload which should be processed with the next retry
     * @param nextTimeout
     * @param ctx         the current RetryContext
     */
    private void persistNewTrigger(String id, String className, RetryTimeout nextTimeout, RetryContext ctx) {

        try{
            this.scheduler.addJob(job(), true);
            this.scheduler.scheduleJob(trigger(id, className, nextTimeout, ctx));
        } catch (SchedulerException ex) {
            throw new QuartzRetryException("Error while scheduling new quartz trigger for retry", ex);
        }
    }

    Optional<RetryContext> findRetryContext(String id) throws SchedulerException {
        Optional<Trigger> trigger = findTrigger(id);
        if (trigger.isPresent()){
            byte[] serializedRetryContext = (byte[]) trigger.get().getJobDataMap().get(DATA_MAP_RETRY_CONTEXT);
            return Optional.of((RetryContext) DeserializerUtil.deserialize(serializedRetryContext));
        }

        return Optional.empty();
    }

    private Optional<Trigger> findTrigger(String id) throws SchedulerException {
        List<? extends Trigger> triggers = scheduler.getTriggersOfJob(new JobKey(JOB_NAME, JOB_GROUP));
        for (Trigger trigger : triggers) {
            if (id.equals(trigger.getJobDataMap().get(DATA_OBJECT_ID))) {
                return Optional.of(trigger);
            }
        }

        return Optional.empty();
    }

    public boolean isRetryExceeded(String id, int maxRetries) throws SchedulerException {
        RetryContext retryContext = findRetryContext(id).orElse(new RetryContext());
        return retryContext.getRetryCount() > maxRetries;
    }
}
