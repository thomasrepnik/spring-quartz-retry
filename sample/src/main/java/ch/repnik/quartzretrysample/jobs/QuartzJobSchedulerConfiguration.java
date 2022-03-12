package ch.repnik.quartzretrysample.jobs;

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
public class QuartzJobSchedulerConfiguration {

    @Value("${suva.cron.schadenmeldung.processor}")
    private String schadenmeldungProcessorCron;

    private static JobDetailFactoryBean createJobDetail(
            Class<? extends Job> jobClass, String jobName) {
        final JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setName(jobName);
        factoryBean.setJobClass(jobClass);
        factoryBean.setDurability(true);
        return factoryBean;
    }

    private static CronTriggerFactoryBean createCronTrigger(
            JobDetail jobDetail, String cronExpression, String triggerName) {
        final CronTriggerFactoryBean factoryBean = new CronTriggerFactoryBean();
        factoryBean.setJobDetail(jobDetail);
        factoryBean.setCronExpression(cronExpression);
        factoryBean.setName(triggerName);
        factoryBean.setMisfireInstruction(CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING);
        return factoryBean;
    }

    @Bean(name = "processorJobDetail")
    public JobDetailFactoryBean cpuJobDetail() {
        return createJobDetail(SampleJob.class, "Processor job");
    }

    @Bean(name = "processorJobTrigger")
    public CronTriggerFactoryBean triggerCpuJob(
            @Qualifier("processorJobDetail") JobDetail jobDetail) {
        return createCronTrigger(
                jobDetail, schadenmeldungProcessorCron, "processor job trigger");
    }
}
