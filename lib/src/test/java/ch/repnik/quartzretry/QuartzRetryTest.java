package ch.repnik.quartzretry;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import javax.sql.DataSource;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;
import static org.quartz.DateBuilder.IntervalUnit.*;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(MockitoExtension.class)
class QuartzRetryTest {

    @Test
    void execute_withoutErros_callsOnSuccess() {

        TestRetrierAdapter testee = new TestRetrierAdapter<String, Integer>() {

            @Override
            protected Integer process(String s, RetryContext ctx) {
                return 15;
            }

            @Override
            protected void onSuccess(String s, Integer integer, RetryContext ctx) {
                assertThat(integer, is(15));
                assertThat(s, is("yolo"));
                assertThat(ctx.getRetryCount(), is(0));
            }

            @Override
            protected void onError(String s, Exception e, RetryContext ctx) {
                Assertions.fail("Should not have been called");
            }

            @Override
            protected void onFailure(String s, Exception e, RetryContext ctx) {
                Assertions.fail("Should not have been called");
            }
        };

        testee.execute("yolo");

    }

    @Test
    void execute_withErros_callsOnError() {

        ApplicationContext appCtx = mock(ApplicationContext.class, RETURNS_DEEP_STUBS);
        when(appCtx.getBean(DataSource.class)).thenReturn(mock(DataSource.class));
        when(appCtx.getEnvironment().getProperty("quartz.retry.disabled", Boolean.class)).thenReturn(false);

        TestRetrierAdapter testee = new TestRetrierAdapter<String, Integer>() {

            @Override
            protected RetryTimeout[] getRetryTimeouts() {
                return new RetryTimeout[]{ RetryTimeout.timeout(2, SECOND) };
            }

            @Override
            protected Integer process(String s, RetryContext ctx) {
                throw new IllegalArgumentException("oops");
            }

            @Override
            protected void onSuccess(String s, Integer integer, RetryContext ctx) {
                Assertions.fail("onSuccess should not have been called");
            }

            @Override
            protected void onError(String s, Exception e, RetryContext ctx) {
                assertThat(s, is("yolo"));
                assertThat(e, instanceOf(IllegalArgumentException.class));
                assertThat(e.getMessage(), is("oops"));
                assertThat(ctx.getRetryCount(), is(0)); //No retry has been initiated so far
            }

            @Override
            protected void onFailure(String s, Exception e, RetryContext ctx) {
                Assertions.fail("onFailure should not have been called");
            }
        };

        testee.setApplicationContext(appCtx);
        testee.setScheduler(mock(Scheduler.class));
        testee.setRetryCount(0);
        testee.setClassname("foo");
        testee.execute("yolo");

    }

    @Test
    void execute_disabled_callsOnFailure() {

        ApplicationContext appCtx = mock(ApplicationContext.class, RETURNS_DEEP_STUBS);
        when(appCtx.getBean(DataSource.class)).thenReturn(mock(DataSource.class));
        when(appCtx.getEnvironment().getProperty("quartz.retry.disabled", Boolean.class)).thenReturn(true);

        TestRetrierAdapter testee = new TestRetrierAdapter<String, Integer>() {

            @Override
            protected RetryTimeout[] getRetryTimeouts() {
                return new RetryTimeout[]{ RetryTimeout.timeout(2, SECOND) };
            }

            @Override
            protected Integer process(String s, RetryContext ctx) {
                throw new IllegalArgumentException("oops");
            }

            @Override
            protected void onSuccess(String s, Integer integer, RetryContext ctx) {
                Assertions.fail("onSuccess should not have been called");
            }

            @Override
            protected void onError(String s, Exception e, RetryContext ctx) {
                Assertions.fail("onError should not have been called");
            }

            @Override
            protected void onFailure(String s, Exception e, RetryContext ctx) {
                assertThat(s, is("yolo"));
                assertThat(ctx.getRetryCount(), is(0)); //Retries are disabled

            }
        };

        testee.setApplicationContext(appCtx);
        testee.setScheduler(mock(Scheduler.class));
        testee.setRetryCount(0);
        testee.setClassname("foo");
        testee.execute("yolo");

    }

    @Test
    void execute_withoutDatabase_callsOnFailure() {

        ApplicationContext appCtx = mock(ApplicationContext.class, RETURNS_DEEP_STUBS);
        when(appCtx.getBean(DataSource.class)).thenThrow(new NoSuchBeanDefinitionException("oops"));
        when(appCtx.getEnvironment().getProperty("quartz.retry.disabled", Boolean.class)).thenReturn(false);

        TestRetrierAdapter testee = new TestRetrierAdapter<String, Integer>() {

            @Override
            protected RetryTimeout[] getRetryTimeouts() {
                return new RetryTimeout[]{ RetryTimeout.timeout(2, SECOND) };
            }

            @Override
            protected Integer process(String s, RetryContext ctx) {
                throw new IllegalArgumentException("oops");
            }

            @Override
            protected void onSuccess(String s, Integer integer, RetryContext ctx) {
                Assertions.fail("onSuccess should not have been called");
            }

            @Override
            protected void onError(String s, Exception e, RetryContext ctx) {
                Assertions.fail("onError should not have been called");
            }

            @Override
            protected void onFailure(String s, Exception e, RetryContext ctx) {
                assertThat(s, is("yolo"));
                assertThat(ctx.getRetryCount(), is(0)); //Retries are disabled

            }
        };

        testee.setApplicationContext(appCtx);
        testee.setScheduler(mock(Scheduler.class));
        testee.setRetryCount(0);
        testee.setClassname("foo");
        testee.execute("yolo");

    }

    @Test
    void execute_withErrosAndNoRetry_callsOnFailure() {

        ApplicationContext appCtx = mock(ApplicationContext.class, RETURNS_DEEP_STUBS);
        when(appCtx.getBean(DataSource.class)).thenReturn(mock(DataSource.class));
        when(appCtx.getEnvironment().getProperty("quartz.retry.disabled", Boolean.class)).thenReturn(false);

        TestRetrierAdapter testee = new TestRetrierAdapter<String, Integer>() {

            @Override
            protected Integer process(String s, RetryContext ctx) {
                throw new IllegalArgumentException("oops");
            }

            @Override
            protected void onSuccess(String s, Integer integer, RetryContext ctx) {
                Assertions.fail("Should not have been called");
            }

            @Override
            protected void onError(String s, Exception e, RetryContext ctx) {
                assertThat(s, is("yolo"));
                assertThat(e, instanceOf(IllegalArgumentException.class));
                assertThat(e.getMessage(), is("oops"));
                assertThat(ctx.getRetryCount(), is(0)); //No retry has been initiated so far
            }

            @Override
            protected void onFailure(String s, Exception e, RetryContext ctx) {
                assertThat(s, is("yolo"));
                assertThat(e, instanceOf(IllegalArgumentException.class));
                assertThat(e.getMessage(), is("oops"));
            }
        };

        testee.setApplicationContext(appCtx);
        testee.setScheduler(mock(Scheduler.class));
        testee.execute("yolo");

    }


    @Test
    void execute_withSchedulerException_throwsException() throws Exception {

        ApplicationContext appCtx = mock(ApplicationContext.class, RETURNS_DEEP_STUBS);
        when(appCtx.getBean(DataSource.class)).thenReturn(mock(DataSource.class));
        when(appCtx.getEnvironment().getProperty("quartz.retry.disabled", Boolean.class)).thenReturn(false);


        TestRetrierAdapter testee = new TestRetrierAdapter<String, Integer>() {

            @Override
            protected RetryTimeout[] getRetryTimeouts() {
                return new RetryTimeout[]{ RetryTimeout.timeout(2, SECOND) };
            }

            @Override
            protected Integer process(String s, RetryContext ctx) {
                throw new IllegalArgumentException("oops");
            }

            @Override
            protected void onSuccess(String s, Integer integer, RetryContext ctx) {
                Assertions.fail("Should not have been called");
            }

            @Override
            protected void onError(String s, Exception e, RetryContext ctx) {

            }

            @Override
            protected void onFailure(String s, Exception e, RetryContext ctx) {
                Assertions.fail("Should not have been called");
            }
        };

        Scheduler scheduler = mock(Scheduler.class);
        doThrow(new SchedulerException("oops")).when(scheduler).addJob(any(), anyBoolean());
        testee.setApplicationContext(appCtx);
        testee.setScheduler(scheduler);

        Assertions.assertThrows(QuartzRetryException.class, () -> testee.execute("yolo"));


    }




}
