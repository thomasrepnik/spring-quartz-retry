package ch.repnik.quartzretry.config;

import ch.repnik.quartzretry.QuartzRetry;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class QuartzRetryPropertiesTest {

    private QuartzRetryProperties testee = new QuartzRetryProperties();

    @Test
    void disabled_setAndGet() {
        testee.setDisabled(false);
        assertThat(testee.isDisabled(), is(false));
    }

}
