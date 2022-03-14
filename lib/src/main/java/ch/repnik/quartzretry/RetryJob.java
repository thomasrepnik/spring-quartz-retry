package ch.repnik.quartzretry;

import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ConfigurableObjectInputStream;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.Serializable;

import static ch.repnik.quartzretry.RetryConstants.*;

/**
 * Internal quartz job which will be executed in case of a firing retry trigger
 */
@Component
@DisallowConcurrentExecution
class RetryJob implements Job {

    private final ApplicationContext ctx;

    @Autowired
    public RetryJob(ApplicationContext ctx){
        this.ctx = ctx;
    }

    /**
     * This method holds the main logic for processing a retry trigger.
     * The last state will be restored and the appropriate bean will be called with the same payload and retryContext.
     * @param jobExecutionContext quartz execution context
     */
    @Override
    public void execute(JobExecutionContext jobExecutionContext) {

        JobDataMap map = jobExecutionContext.getMergedJobDataMap();
        String className = map.get(DATA_MAP_CLASSNAME).toString();
        int retryCount = (int) map.get(DATA_MAP_RETRY_COUNT);
        byte[] payload = (byte[])map.get(DATA_MAP_PAYLOAD);
        byte[] serializedRetryContext = (byte[]) map.get(DATA_MAP_RETRY_CONTEXT);

        Serializable deserialized = (Serializable) deserialize(payload);
        RetryContext retryContext = (RetryContext) deserialize(serializedRetryContext);

        try {
            @SuppressWarnings("unchecked") QuartzRetry<Serializable, ?> bean = (QuartzRetry<Serializable, ?>) ctx.getBean(className);
            bean.setRetryCount(++retryCount);
            bean.execute(deserialized, retryContext);
        } catch (Exception e) {
            throw new QuartzRetryException("Could not create bean " + className, e);
        }

    }

    /**
     * This method is needed to support compatibility with spring devtools (because they use another classloader)
     * see https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#using.devtools.restart.customizing-the-classload
     * @param in serialized byteArray
     * @return deserialized Object
     */
    protected Object deserialize(final byte[] in) {
        if (in == null){
            return null;
        }

        Object o;
        try (
            ByteArrayInputStream bis = new ByteArrayInputStream(in);
            ConfigurableObjectInputStream is = new ConfigurableObjectInputStream(bis, Thread.currentThread().getContextClassLoader())
        ) {
            o = is.readObject();
        } catch (Exception e) {
            throw new QuartzRetryException("Could not deserialize object from quartz jobDataMap", e);
        }
        return o;
    }

}
