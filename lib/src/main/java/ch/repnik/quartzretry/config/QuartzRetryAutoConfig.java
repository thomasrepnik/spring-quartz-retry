package ch.repnik.quartzretry.config;

import org.quartz.Trigger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import javax.sql.DataSource;

@Configuration
@ConditionalOnBean(DataSource.class)
@EnableConfigurationProperties(QuartzRetryProperties.class)
public class QuartzRetryAutoConfig {

    @Bean
    @ConditionalOnMissingBean
    public SchedulerFactoryBean scheduler(DataSource dataSource, SpringBeanJobFactory jobFactory, Trigger... triggers) {
        final SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();

        schedulerFactory.setOverwriteExistingJobs(false);
        schedulerFactory.setAutoStartup(true);
        schedulerFactory.setDataSource(dataSource);
        schedulerFactory.setJobFactory(jobFactory);
        schedulerFactory.setWaitForJobsToCompleteOnShutdown(true);
        if (triggers != null && triggers.length > 0) {
            schedulerFactory.setTriggers(triggers);
        }
        return schedulerFactory;
    }

    @Bean
    @ConditionalOnMissingBean
    public SpringBeanJobFactory springBeanJobFactory(ApplicationContext context) {
        final SpringBeanJobFactory jobFactory = new SpringBeanJobFactory();
        jobFactory.setApplicationContext(context);
        return jobFactory;
    }

}
