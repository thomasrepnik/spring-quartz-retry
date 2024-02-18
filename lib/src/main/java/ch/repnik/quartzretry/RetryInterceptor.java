package ch.repnik.quartzretry;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class RetryInterceptor implements MethodInterceptor {


    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {

        System.out.println("intercepting method " + invocation.getMethod().getName());
        return invocation.proceed();

        /*switch (invocation.getMethod().getName()) {
            case "execute":
                return "processed " + invocation.getArguments()[0];
            case "process":
                return (int) invocation.getArguments()[0] + 2;
            default:
                return invocation.proceed();
        }*/

    }



}
