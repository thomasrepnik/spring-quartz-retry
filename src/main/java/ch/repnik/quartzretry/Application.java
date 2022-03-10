package ch.repnik.quartzretry;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        //System.setProperty("spring.devtools.restart.enabled", "false");

        SpringApplication.run(Application.class, args);
    }

}
