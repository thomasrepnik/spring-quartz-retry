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
        String id = map.get(DATA_OBJECT_ID).toString();
        int retryCount = (int) map.get(DATA_MAP_RETRY_COUNT);
        byte[] payload = (byte[])map.get(DATA_MAP_PAYLOAD);
        byte[] serializedRetryContext = (byte[]) map.get(DATA_MAP_RETRY_CONTEXT);

        Serializable deserialized = (Serializable) DeserializerUtil.deserialize(payload);
        RetryContext retryContext = (RetryContext) DeserializerUtil.deserialize(serializedRetryContext);


        @SuppressWarnings("unchecked") QuartzRetriable<?> bean = (QuartzRetriable<?>) createBean(className);
        executeWithCapture(bean, id);

    }

    // Helper method to capture the wildcard
    private <T> void executeWithCapture(QuartzRetriable<T> bean, String id) {
        T payload = bean.selectById(id);
        bean.execute(payload, id);
    }

    /**
     * Searches and creates a bean instance based on the classname
     * @param className classname of the bean
     * @return the bean instance
     */
    private Object createBean(String className){
        Class<?> resolvedClass;

        try{
            resolvedClass = Class.forName(className);
            return ctx.getBean(resolvedClass);
        }catch(ClassNotFoundException e){
            return ctx.getBean(className); //Fallback for testing
        }
    }

    /**
     * This method is needed to support compatibility with spring devtools (because they use another classloader)
     * see https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#using.devtools.restart.customizing-the-classload
     * @param in serialized byteArray
     * @return deserialized Object
     */


}
