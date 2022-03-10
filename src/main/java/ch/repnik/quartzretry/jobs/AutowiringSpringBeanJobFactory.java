package ch.repnik.quartzretry.jobs;

import org.quartz.Job;
import org.quartz.SchedulerContext;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

/**
 * Adds autowiring support to quartz jobs. (more info at: https://gist.github.com/jelies/5085593)
 */
public final class AutowiringSpringBeanJobFactory extends SpringBeanJobFactory
        implements ApplicationContextAware {

    private ApplicationContext ctx;
    private SchedulerContext schedulerContext;

    @Override
    public void setApplicationContext(final ApplicationContext context) {
        this.ctx = context;
    }

    @Override
    protected Object createJobInstance(final TriggerFiredBundle bundle) throws Exception {
        final Job job = ctx.getBean(bundle.getJobDetail().getJobClass());
        final BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(job);
        final MutablePropertyValues pvs = new MutablePropertyValues();
        pvs.addPropertyValues(bundle.getJobDetail().getJobDataMap());
        pvs.addPropertyValues(bundle.getTrigger().getJobDataMap());

        if (this.schedulerContext != null) {
            pvs.addPropertyValues(this.schedulerContext);
        }

        bw.setPropertyValues(pvs, true);
        return job;
    }

    @Override
    public void setSchedulerContext(SchedulerContext schedulerContext) {
        this.schedulerContext = schedulerContext;
        super.setSchedulerContext(schedulerContext);
    }
}
