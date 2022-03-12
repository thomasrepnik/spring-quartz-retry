package ch.repnik.quartzretrysample.jobs.controller;

import ch.repnik.quartzretrysample.service.SampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SampleController {

    @Autowired
    private SampleService service;

    @GetMapping("/call")
    private ResponseEntity call(){
        service.process();
        return ResponseEntity.ok().build();
    }

}
