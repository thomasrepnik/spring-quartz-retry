package ch.repnik.quartzretry.config;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import javax.sql.DataSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;


class QuartzRetryAutoConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(QuartzRetryAutoConfig.class));

    @Test
    @Disabled
    void foo() {
        this.contextRunner
                .withClassLoader(new FilteredClassLoader(DataSource.class))
                .run((context) -> {
            assertThat(context.containsBean("SchedulerFactoryBean"), is(true));
        });
    }

}
