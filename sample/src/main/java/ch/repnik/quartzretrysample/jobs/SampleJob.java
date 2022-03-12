package ch.repnik.quartzretrysample.jobs;

import ch.repnik.quartzretrysample.service.SampleService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

@Component
@DisallowConcurrentExecution
@ComponentScan("ch.repnik.quartzretry")
public class SampleJob implements Job {

    @Autowired private SampleService sampleService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {

        //sampleService.process();

    }
}
