package ch.repnik.quartzretry;

import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ConfigurableObjectInputStream;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.Serializable;


@Component
@DisallowConcurrentExecution
public class RetryJob implements Job {

    private ApplicationContext ctx;

    @Autowired
    public RetryJob(ApplicationContext ctx){
        this.ctx = ctx;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        JobDataMap map = jobExecutionContext.getMergedJobDataMap();
        String className = map.get("classname").toString();
        int retryCount = (int) map.get("retryCount");
        byte[] payload = (byte[])map.get("payload");

        Serializable deserialized = (Serializable) deserialize(payload);

        try {
            Class<?> retryImpl = Class.forName(className);
            AbstractRetrier bean = (AbstractRetrier) ctx.getBean(retryImpl);
            bean.setRetryCount(++retryCount);
            bean.start(deserialized);
        } catch (ClassNotFoundException e) {
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
            throw new QuartzRetryException("Could not deserialize object from quartz jobDataMap");
        }
        return o;
    }


}
