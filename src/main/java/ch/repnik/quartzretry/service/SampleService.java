package ch.repnik.quartzretry.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SampleService {

    @Autowired
    Caller caller;


    public void process() {
        Entity entity = new Entity();
        caller.start(entity);
    }

}
