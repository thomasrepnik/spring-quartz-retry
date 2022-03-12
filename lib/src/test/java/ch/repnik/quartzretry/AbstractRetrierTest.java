package ch.repnik.quartzretry;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.Scheduler;

import static ch.repnik.quartzretry.RetryInterval.retry;
import static org.mockito.Mockito.mock;
import static org.quartz.DateBuilder.IntervalUnit.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

@ExtendWith(MockitoExtension.class)
class AbstractRetrierTest {

    @Test
    void startAttempt_withoutErros_callsOnSuccess() {

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

        testee.startAttempt("yolo");

    }

    @Test
    void startAttempt_withErros_callsOnError() {

        TestRetrierAdapter testee = new TestRetrierAdapter<String, Integer>() {

            @Override
            protected RetryInterval[] getRetryInterval() {
                return new RetryInterval[]{ retry(2, SECOND) };
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
                assertThat(s, is("yolo"));
                assertThat(e, instanceOf(IllegalArgumentException.class));
                assertThat(e.getMessage(), is("oops"));
                assertThat(ctx.getRetryCount(), is(0)); //No retry has been initiated so far
            }

            @Override
            protected void onFailure(String s, Exception e, RetryContext ctx) {
                Assertions.fail("Should not have been called");
            }
        };

        testee.setScheduler(mock(Scheduler.class));
        testee.startAttempt("yolo");

    }

    @Test
    void startAttempt_withErrosAndNoRetry_callsOnFailure() {

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
                Assertions.fail("Should not have been called");
            }

            @Override
            protected void onFailure(String s, Exception e, RetryContext ctx) {
                assertThat(s, is("yolo"));
                assertThat(e, instanceOf(IllegalArgumentException.class));
                assertThat(e.getMessage(), is("oops"));
            }
        };

        testee.setScheduler(mock(Scheduler.class));
        testee.startAttempt("yolo");

    }

}
