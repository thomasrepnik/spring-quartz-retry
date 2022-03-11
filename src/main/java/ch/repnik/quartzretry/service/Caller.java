package ch.repnik.quartzretry.service;

import ch.repnik.quartzretry.retry.AbstractRetrier;
import ch.repnik.quartzretry.retry.RetryInterval;
import org.quartz.DateBuilder;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.stereotype.Component;

import java.util.Date;

import static ch.repnik.quartzretry.retry.RetryInterval.retry;
import static org.quartz.DateBuilder.IntervalUnit.*;

@Component
public class Caller extends AbstractRetrier<Entity, String> {

    @Override
    protected String call(Entity entity) {
        System.out.println(entity.getName() + " wird an rimex gesendet (Aktueller State: " + entity.getState() +")");

        if (entity.getRetryCount() <= 2) {
            throw new IllegalArgumentException("Service Call war nicht erfolgreich");
        }

        return entity.getName() + " Rimex Upload erfolgreich am " + new Date();
    }

    @Override
    protected RetryInterval[] getRetryInterval() {
        return new RetryInterval[] {
                retry(1, SECOND),
                retry(1, SECOND)
        };
    }

    @Override
    protected void onError(Entity entity, Exception e) {
        System.out.println(entity.getName() + " Fehler Nr" + entity.getRetryCount());
        entity.setRetryCount(entity.getRetryCount()+1);
        entity.setState(RetryState.RETRY);
    }

    @Override
    protected void onSuccess(Entity entity, String s) {
        entity.setState(RetryState.SUCCESS);
        System.out.println(entity.getName() + " Erfolgreich: " + s);
    }

    @Override
    protected void onFailure(Entity entity, Exception e) {
        System.out.println("Alle Retries Fehlgeschlagen.");
        e.printStackTrace();
    }

}
