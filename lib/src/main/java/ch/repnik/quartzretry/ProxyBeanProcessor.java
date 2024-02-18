package ch.repnik.quartzretry;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


// see http://www.javabyexamples.com/quick-guide-to-spring-beanpostprocessor
// https://blog.devgenius.io/demystifying-proxy-in-spring-3ab536046b11
// https://blog.pchudzik.com/201906/proxy-factory/



//@Component
public class ProxyBeanProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof QuartzRetriable) {
            System.out.println("postProcessBeforeInitialization");

            return proxiedBean((QuartzRetriable) bean);

            // Create a ProxyFactory
            /*ProxyFactory proxyFactory = new ProxyFactory(bean);

            // Add your advice or interceptor
            RetryInterceptor retryInterceptor = new RetryInterceptor();
            proxyFactory.addAdvice(retryInterceptor);

            // Create the proxy and return it
            return proxyFactory.getProxy();*/
        }

        // If the bean is not an instance of MyTargetClass, return it as is
        return bean;
    }

    private Object proxiedBean(QuartzRetriable bean) {
        ProxyFactory proxyFactory = new ProxyFactory(bean);
        //proxyFactory.setTargetClass(bean.getClass());
        proxyFactory.addAdvice(new RetryInterceptor());
        return proxyFactory.getProxy();
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // No additional processing needed after initialization
        return bean;
    }

    // Rest of your classes remain the same...
}

