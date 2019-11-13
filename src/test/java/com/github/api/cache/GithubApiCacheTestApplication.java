package com.github.api.cache;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;

@Profile("test")
@SpringBootApplication(scanBasePackages = {"com.github.api.cache.service", "com.github.api.cache.service"})
public class GithubApiCacheTestApplication {

    public static void main(String[] args) {

        SpringApplication.run(GithubApiCacheTestApplication.class, args);
    }
}
