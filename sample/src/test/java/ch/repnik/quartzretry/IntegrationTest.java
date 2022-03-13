package ch.repnik.quartzretry;

import ch.repnik.quartzretry.AbstractRetrier;
import ch.repnik.quartzretry.RetryContext;
import ch.repnik.quartzretry.RetryInterval;
import ch.repnik.quartzretrysample.service.Caller;
import ch.repnik.quartzretrysample.service.Payload;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Spy;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;

import java.util.List;

import static ch.repnik.quartzretry.RetryInterval.retry;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
public class IntegrationTest {

    @Autowired
    private GenericWebApplicationContext ctx;

    @Autowired
    private Scheduler scheduler;

    @Test
    void executeQuartzRetry_multipleRetries_success() throws Exception {

        //Arrange
        AbstractRetrier<String, String> retrier = new RetrierAdapter<>() {
            @Override
            protected String process(String payload, RetryContext ctx) {
                switch (ctx.getRetryCount()) {
                    case 0:
                    case 1:
                        throw new IllegalArgumentException("oops");
                }

                return "success";
            }

            @Override
            protected RetryInterval[] getRetryInterval() {
                return new RetryInterval[]{
                        retry(1, DateBuilder.IntervalUnit.SECOND),
                        retry(1, DateBuilder.IntervalUnit.SECOND),
                        retry(1, DateBuilder.IntervalUnit.SECOND)
                };
            }
        };


        AbstractRetrier<String, String> spiedRetrier = spy(retrier);

        ctx.registerBean("IntegrationTestRetrier", AbstractRetrier.class, () -> spiedRetrier);
        spiedRetrier.setClassname("IntegrationTestRetrier");
        spiedRetrier.setScheduler(scheduler);

        //Act
        spiedRetrier.startAttempt("let's go");
        Thread.sleep(3000);

        //Assert
        InOrder inOrder = inOrder(spiedRetrier);
        inOrder.verify(spiedRetrier).onError(eq("let's go"), any(), any());
        inOrder.verify(spiedRetrier).onSuccess(eq("let's go"), eq("success"), any());

        verify(spiedRetrier, never()).onFailure(anyString(), any(), any());

        assertThat(getRetryTriggers(), is(empty()));

    }

    @Test
    void executeQuartzRetry_multipleRetries_failure() throws Exception {

        //Arrange
        AbstractRetrier<String, String> retrier = new RetrierAdapter<>() {
            @Override
            protected String process(String payload, RetryContext ctx) {
                throw new IllegalArgumentException("oops");
            }

            @Override
            protected RetryInterval[] getRetryInterval() {
                return new RetryInterval[]{
                        retry(1, DateBuilder.IntervalUnit.SECOND),
                        retry(1, DateBuilder.IntervalUnit.SECOND)
                };
            }
        };


        AbstractRetrier<String, String> spiedRetrier = spy(retrier);

        ctx.registerBean("IntegrationTestRetrier2", AbstractRetrier.class, () -> spiedRetrier);
        spiedRetrier.setClassname("IntegrationTestRetrier2");
        spiedRetrier.setScheduler(scheduler);

        //Act
        spiedRetrier.startAttempt("let's go");
        Thread.sleep(3000);

        //Assert
        InOrder inOrder = inOrder(spiedRetrier);
        inOrder.verify(spiedRetrier).onError(eq("let's go"), any(), any());
        inOrder.verify(spiedRetrier).onError(eq("let's go"), any(), any());
        inOrder.verify(spiedRetrier).onFailure(eq("let's go"), any(), any());

        verify(spiedRetrier, never()).onSuccess(anyString(), any(), any());

        assertThat(getRetryTriggers(), is(empty()));

    }

    @Test
    void executeQuartzRetry_correktMisfireInstructions_triggersExecuted() throws Exception {

        //Arrange
        AbstractRetrier<String, String> retrier = new RetrierAdapter<>() {
            @Override
            protected String process(String payload, RetryContext ctx) {
                throw new IllegalArgumentException("oops");
            }

            @Override
            protected RetryInterval[] getRetryInterval() {
                return new RetryInterval[]{
                        retry(2, DateBuilder.IntervalUnit.SECOND),
                        retry(1, DateBuilder.IntervalUnit.SECOND)
                };
            }
        };


        AbstractRetrier<String, String> spiedRetrier = spy(retrier);

        ctx.registerBean("IntegrationTestRetrier3", AbstractRetrier.class, () -> spiedRetrier);
        spiedRetrier.setClassname("IntegrationTestRetrier3");
        spiedRetrier.setScheduler(scheduler);

        //Act
        spiedRetrier.startAttempt("let's go");
        Thread.sleep(500);

        assertThat(getRetryTriggers(), hasSize(1));
        scheduler.pauseAll();
        Thread.sleep(2500); //Wait until the first trigger has been expired
        scheduler.resumeAll(); //This will execute the misfire instructions

        Thread.sleep(1500); //Wait for the second trigger to execute
        //Assert

        InOrder inOrder = inOrder(spiedRetrier);
        inOrder.verify(spiedRetrier).onError(eq("let's go"), any(), any());
        inOrder.verify(spiedRetrier).onError(eq("let's go"), any(), any());
        inOrder.verify(spiedRetrier).onFailure(eq("let's go"), any(), any());

        verify(spiedRetrier, never()).onSuccess(anyString(), any(), any());

        assertThat(getRetryTriggers(), is(empty()));

    }

    private List<? extends Trigger> getRetryTriggers() throws SchedulerException {
        return scheduler.getTriggersOfJob(new JobKey(RetryConstants.JOB_NAME, RetryConstants.JOB_GROUP));
    }

}
