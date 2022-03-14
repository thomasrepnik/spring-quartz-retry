package ch.repnik.quartzretry;

import org.quartz.*;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ConfigurableObjectInputStream;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.Serializable;

import static ch.repnik.quartzretry.RetryConstants.*;


@Component
@DisallowConcurrentExecution
class RetryJob implements Job {

    private ApplicationContext ctx;

    @Autowired
    public RetryJob(ApplicationContext ctx){
        this.ctx = ctx;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        JobDataMap map = jobExecutionContext.getMergedJobDataMap();
        String className = map.get(DATA_MAP_CLASSNAME).toString();
        int retryCount = (int) map.get(DATA_MAP_RETRY_COUNT);
        byte[] payload = (byte[])map.get(DATA_MAP_PAYLOAD);
        byte[] serializedRetryContext = (byte[]) map.get(DATA_MAP_RETRY_CONTEXT);

        Serializable deserialized = (Serializable) deserialize(payload);
        RetryContext retryContext = (RetryContext) deserialize(serializedRetryContext);

        try {
            QuartzRetry bean = (QuartzRetry) ctx.getBean(className);
            bean.setRetryCount(++retryCount);
            bean.execute(deserialized, retryContext);
        } catch (NoSuchBeanDefinitionException e) {
            throw new QuartzRetryException("Could not create bean " + className, e);
        }

    }

    /**
     * Wird benötigt damit es mit den Spring DevTools funktioniert (da dort ein anderer Classloader verwendet wird)
     * Siehe auch https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#using.devtools.restart.customizing-the-classload
     * @param in
     * @return
     */
    protected Object deserialize(final byte[] in) {
        if (in == null){
            return null;
        }

        Object o = null;
        try (
            ByteArrayInputStream bis = new ByteArrayInputStream(in);
            ConfigurableObjectInputStream is = new ConfigurableObjectInputStream(bis, Thread.currentThread().getContextClassLoader());
        ) {
            o = is.readObject();
        } catch (Exception e) {
            throw new QuartzRetryException("Could not deserialize object from quartz jobDataMap", e);
        }
        return o;
    }


}
