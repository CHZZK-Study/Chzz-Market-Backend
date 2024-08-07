package org.chzz.market.common;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class AWSConfig {
    @Bean
    @Primary
    public AmazonS3 amazonS3() {
        return Mockito.mock(AmazonS3.class);
    }

    @Bean
    @Primary
    public AWSCredentialsProvider awsCredentialsProvider() {
        return Mockito.mock(AWSCredentialsProvider.class);
    }

    @Bean
    @Primary
    public String s3BucketName() {
        return "test-bucket";
    }
}
