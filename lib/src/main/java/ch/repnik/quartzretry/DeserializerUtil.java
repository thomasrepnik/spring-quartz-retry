package ch.repnik.quartzretry;

import org.springframework.core.ConfigurableObjectInputStream;

import java.io.ByteArrayInputStream;

public class DeserializerUtil {

    public static Object deserialize(final byte[] in) {
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
