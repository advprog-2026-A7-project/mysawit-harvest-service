package com.mysawit.harvest.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import software.amazon.awssdk.services.s3.S3Client;

import static org.assertj.core.api.Assertions.assertThat;

class SupabaseStorageConfigTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(SupabaseStorageConfig.class);

    @Test
    void shouldCreateS3ClientBeanWithCorrectConfiguration() {
        this.contextRunner
                .withPropertyValues(
                        "supabase.storage.endpoint=https://xyz.supabase.co/storage/v1/s3",
                        "supabase.storage.region=ap-southeast-1",
                        "supabase.storage.access-key=mock-access-key",
                        "supabase.storage.secret-key=mock-secret-key"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(S3Client.class);

                    S3Client s3Client = context.getBean(S3Client.class);
                    assertThat(s3Client).isNotNull();
                });
    }

    @Test
    void shouldFailWhenPropertiesAreMissing() {
        this.contextRunner
                .run(context -> {
                    assertThat(context.getStartupFailure()).isNotNull();
                });
    }
}