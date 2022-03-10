package ch.repnik.quartzretry.jobs;

import ch.repnik.quartzretry.SampleService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@DisallowConcurrentExecution
public class SampleJob implements Job {

    @Autowired private SampleService sampleService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {

        //sampleService.process();

    }
}
