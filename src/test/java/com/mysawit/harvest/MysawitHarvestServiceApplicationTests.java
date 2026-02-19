package com.mysawit.harvest;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class MysawitHarvestServiceApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void mainRunsWithTestProfile() {
        MysawitHarvestServiceApplication.main(
            new String[] {
                "--spring.profiles.active=test",
                "--spring.main.web-application-type=none",
                "--app.test.close-context=true"
            }
        );
    }
}
