package ch.repnik.quartzretry;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class RetryContextTest {

    private RetryContext testee = new RetryContext();

    @Test
    void setAndGetRetryCount_returnsCorrentValue() {
        testee.setRetryCount(3);
        assertThat(testee.getRetryCount(), is(3));
    }

    @Test
    void getDataMap_returnsCorrentValue() {
        assertThat(testee.getDataMap().isEmpty(), is(true));
    }

}
