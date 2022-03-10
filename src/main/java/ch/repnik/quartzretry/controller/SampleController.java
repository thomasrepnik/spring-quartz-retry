package ch.repnik.quartzretry.controller;

import ch.repnik.quartzretry.service.SampleService;
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
