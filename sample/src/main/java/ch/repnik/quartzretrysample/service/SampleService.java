package ch.repnik.quartzretrysample.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SampleService {

    @Autowired
    Caller caller;


    public void process() {
        Payload entity = new Payload();
        caller.startAttempt(entity);
    }

}
