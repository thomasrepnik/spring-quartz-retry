package ch.repnik.quartzretry;

import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class Caller extends AbstractRetrier<Entity, String> {

    @Override
    protected String call(Entity entity) {

        System.out.println(entity.getName() + " wird an rimex gesendet");

        if (entity.getRetryCount() <= 2) {
            throw new IllegalArgumentException("Service Call war nicht erfolgreich");
        }

        return entity.getName() + " Rimex Upload erfolgreich am " + new Date();
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

}
