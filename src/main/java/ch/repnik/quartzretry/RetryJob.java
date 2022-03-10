package ch.repnik.quartzretry;

import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.quartz.*;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ConfigurableObjectInputStream;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;


@Component
@Slf4j
@DisallowConcurrentExecution
public class RetryJob implements Job {

    @Autowired
    private ApplicationContext ctx;


    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        JobDataMap map = jobExecutionContext.getMergedJobDataMap();
        String className = map.get("classname").toString();
        byte[] payload = (byte[])map.get("payload");

        Serializable deserialized = (Serializable) deserialize(payload);

        try {
            Class<?> retryImpl = Class.forName(className);
            AbstractRetrier bean = (AbstractRetrier) ctx.getBean(retryImpl);
            //System.out.println("Retry wird duchgeführt: "  + className);
            bean.start(deserialized);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }



    }

    /**
     * Wird benötigt damit es mit den Spring DevTools funktioniert (da dort ein anderer Classloader verwendet wird)
     * Siehe auch https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#using.devtools.restart.customizing-the-classload
     * @param in
     * @return
     */
    protected Object deserialize(final byte[] in) {
        Object o = null;
        ByteArrayInputStream bis = null;
        ConfigurableObjectInputStream is = null;
        try {
            if (in != null) {
                bis = new ByteArrayInputStream(in);
                is = new ConfigurableObjectInputStream(bis, Thread.currentThread().getContextClassLoader());
                o = is.readObject();
                is.close();
                bis.close();
            }
        } catch (IOException e) {
        } catch (ClassNotFoundException e) {
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(bis);
        }
        return o;
    }


}
