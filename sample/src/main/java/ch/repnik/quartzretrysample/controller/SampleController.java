package ch.repnik.quartzretrysample.controller;

import ch.repnik.quartzretrysample.service.Payload;
import ch.repnik.quartzretrysample.service.SampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SampleController {

    @Autowired
    private SampleService sampleService;

    @GetMapping("/start")
    private ResponseEntity call(){
        sampleService.execute(new Payload(), "123");
        return ResponseEntity.ok().build();
    }

}
