package ch.repnik.quartzretry.jobs;

import lombok.extern.slf4j.Slf4j;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;

@Configuration
@Slf4j
public class QuartzJobSchedulerConfiguration {

    @Value("${suva.cron.schadenmeldung.processor}")
    private String schadenmeldungProcessorCron;

    private static JobDetailFactoryBean createJobDetail(
            Class<? extends Job> jobClass, String jobName) {
        log.debug("createJobDetail(jobClass={}, jobName={})", jobClass.getName(), jobName);
        final JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setName(jobName);
        factoryBean.setJobClass(jobClass);
        factoryBean.setDurability(true);
        return factoryBean;
    }

    private static CronTriggerFactoryBean createCronTrigger(
            JobDetail jobDetail, String cronExpression, String triggerName) {
        log.debug(
                "createCronTrigger(jobDetail={}, cronExpression={}, triggerName={})",
                jobDetail.toString(),
                cronExpression,
                triggerName);
        final CronTriggerFactoryBean factoryBean = new CronTriggerFactoryBean();
        factoryBean.setJobDetail(jobDetail);
        factoryBean.setCronExpression(cronExpression);
        factoryBean.setName(triggerName);
        factoryBean.setMisfireInstruction(CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING);
        return factoryBean;
    }

    @Bean(name = "schadenmeldungProcessorJobDetail")
    public JobDetailFactoryBean cpuJobDetail() {
        return createJobDetail(SampleJob.class, "SchadenmeldungProcessor job");
    }

    @Bean(name = "schadenmeldungProcessorJobTrigger")
    public CronTriggerFactoryBean triggerCpuJob(
            @Qualifier("schadenmeldungProcessorJobDetail") JobDetail jobDetail) {
        return createCronTrigger(
                jobDetail, schadenmeldungProcessorCron, "SchadenmeldungProcessor job trigger");
    }
}
