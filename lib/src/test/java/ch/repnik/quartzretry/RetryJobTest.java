package ch.repnik.quartzretry;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.util.SerializationUtils;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RetryJobTest {

    @Mock
    private ApplicationContext ctx;

    @InjectMocks
    private RetryJob testee;

    @Test
    void execute_allValid_callsBeanMethods() {
        //Arrange
        JobExecutionContext jobContext = mock(JobExecutionContext.class);
        JobDataMap map = new JobDataMap();
        map.put(RetryConstants.DATA_MAP_RETRY_COUNT, 3);
        map.put(RetryConstants.DATA_MAP_CLASSNAME, "fooBean");
        map.put(RetryConstants.DATA_MAP_PAYLOAD, SerializationUtils.serialize("payload"));
        RetryContext retryContext = new RetryContext();
        retryContext.getDataMap().put("foo", "bar");
        map.put(RetryConstants.DATA_MAP_RETRY_CONTEXT, SerializationUtils.serialize(retryContext));

        when(jobContext.getMergedJobDataMap()).thenReturn(map);

        QuartzRetry quartzRetry = spy(new TestRetrierAdapter<>());
        when(ctx.getBean("fooBean")).thenReturn(quartzRetry);

        //Act
        testee.execute(jobContext);

        //Assert
        ArgumentCaptor<RetryContext> captor = ArgumentCaptor.forClass(RetryContext.class);
        verify(quartzRetry).setRetryCount(4);
        verify(quartzRetry).execute(eq("payload"), captor.capture());
        assertThat(captor.getValue().getDataMap().entrySet(), everyItem(isIn(Collections.singletonMap("foo", "bar").entrySet())));
    }


    @Test
    void execute_beanNotFound_throwsException() {
        //Arrange
        JobExecutionContext jobContext = mock(JobExecutionContext.class);
        JobDataMap map = new JobDataMap();
        map.put(RetryConstants.DATA_MAP_RETRY_COUNT, 3);
        map.put(RetryConstants.DATA_MAP_CLASSNAME, "fooBean");
        map.put(RetryConstants.DATA_MAP_PAYLOAD, SerializationUtils.serialize("payload"));
        map.put(RetryConstants.DATA_MAP_RETRY_CONTEXT, SerializationUtils.serialize(new RetryContext()));
        when(jobContext.getMergedJobDataMap()).thenReturn(map);
        when(ctx.getBean("fooBean")).thenThrow(new NoSuchBeanDefinitionException("oops"));

        //Act
        Assertions.assertThrows(QuartzRetryException.class, () -> testee.execute(jobContext));

    }

    @Test
    void deserialize_returnsCorrectString() {
        //Arrange
        byte[] bytes = SerializationUtils.serialize("secret");
        //Act
        Object result = testee.deserialize(bytes);
        //Assert
        assertThat(result, is("secret"));
    }

    @Test
    void deserialize_failing_throwsException() {
        //Arrange
        byte[] bytes = SerializationUtils.serialize("secret");
        //Act & Assert
        Assertions.assertThrows(QuartzRetryException.class, () -> testee.deserialize(new byte[10]));

    }

    @Test
    void deserialize_null_returnsNull() {
        //Act & Assert
        assertThat(testee.deserialize(null), is(nullValue()));
    }

}
