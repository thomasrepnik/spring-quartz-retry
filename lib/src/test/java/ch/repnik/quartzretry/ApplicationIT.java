package ch.repnik.quartzretry;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApplicationIT {

    @Test
    void load_startupSuccessful() {
        Assertions.assertTrue(true);
    }
}
