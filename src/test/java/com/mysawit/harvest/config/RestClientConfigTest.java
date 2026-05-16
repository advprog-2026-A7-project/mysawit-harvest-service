package com.mysawit.harvest.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

class RestClientConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RestClientConfig.class));

    @Test
    void restClientBuilderBeanShouldBePresent() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(RestClient.Builder.class);

            RestClient.Builder builder = context.getBean(RestClient.Builder.class);
            assertThat(builder).isNotNull();
        });
    }
}