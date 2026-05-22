package com.mysawit.harvest;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class MysawitHarvestServiceApplication {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

        ConfigurableApplicationContext context = SpringApplication.run(MysawitHarvestServiceApplication.class, args);

        if (context.getEnvironment().getProperty("app.test.close-context", Boolean.class, false)) {
            context.close();
        }
    }
}