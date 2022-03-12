package ch.repnik.quartzretrysample.jobs;

import org.quartz.Trigger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import javax.sql.DataSource;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Configuration
public class QuartzConfiguration {

    public static final String DISABLE_ALL_CRONJOBS_KEY = "suva.cronjobs.disableall";
    private static final String QUARTZ_SPRING_PREFIX = "spring.quartz.properties.";
    private ApplicationContext applicationContext;
    private DataSource dataSource;

    public QuartzConfiguration(ApplicationContext applicationContext, DataSource dataSource) {
        this.applicationContext = applicationContext;
        this.dataSource = dataSource;
    }

    @Bean
    public SchedulerFactoryBean scheduler(Trigger... triggers) {
        final SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();

        final List<MapPropertySource> propertySources = getPropertySources();
        final Trigger[] jobsToStart = filterTriggers(applicationContext.getEnvironment(), triggers);

        final Properties properties = getQuarzProperties(propertySources);

        schedulerFactory.setOverwriteExistingJobs(true);
        schedulerFactory.setAutoStartup(true);
        schedulerFactory.setDataSource(dataSource);
        schedulerFactory.setQuartzProperties(properties);
        schedulerFactory.setJobFactory(springBeanJobFactory());
        schedulerFactory.setWaitForJobsToCompleteOnShutdown(true);
        if (jobsToStart.length > 0) {
            schedulerFactory.setTriggers(jobsToStart);
        }
        return schedulerFactory;
    }

    private Trigger[] filterTriggers(final Environment env, final Trigger[] triggers) {
        if (triggers == null || triggers.length == 0) {
            return new Trigger[0];
        }

        if (env.getProperty(DISABLE_ALL_CRONJOBS_KEY, Boolean.class, false)) {
            return new Trigger[0];
        } else {
            return triggers;
        }
    }

    private Properties getQuarzProperties(final List<MapPropertySource> propertySources) {
        // Alle Konfigurationen von Quartz auslesen und in den Properties speichern
        final Properties properties = new Properties();
        for (MapPropertySource source : propertySources) {
            source.getSource().entrySet().stream()
                    .filter(e -> e.getKey().startsWith(QuartzConfiguration.QUARTZ_SPRING_PREFIX))
                    .forEach(
                            entry ->
                                    properties.setProperty(
                                            entry.getKey().replace(QuartzConfiguration.QUARTZ_SPRING_PREFIX, ""),
                                            entry.getValue().toString()));
        }
        return properties;
    }

    private List<MapPropertySource> getPropertySources() {
        final List<MapPropertySource> propertySources = new ArrayList<>();

        // Alle PropertySources von Spring auslesen und in Umgekehrte Reihenfolge bringen (niedriste
        // Prio ist zuoberst in der Liste)
        for (PropertySource<?> propertySource :
                ((AbstractEnvironment) applicationContext.getEnvironment()).getPropertySources()) {
            if (propertySource instanceof MapPropertySource) {
                propertySources.add(0, (MapPropertySource) propertySource);
            }
        }
        return propertySources;
    }

    @Bean
    public SpringBeanJobFactory springBeanJobFactory() {
        final AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        return jobFactory;
    }


}
