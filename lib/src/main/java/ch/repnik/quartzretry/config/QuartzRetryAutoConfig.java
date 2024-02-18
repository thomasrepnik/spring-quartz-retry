package ch.repnik.quartzretry.config;

import ch.repnik.quartzretry.ProxyBeanProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@ConditionalOnBean(DataSource.class)
@EnableConfigurationProperties(QuartzRetryProperties.class)
public class QuartzRetryAutoConfig {


}
