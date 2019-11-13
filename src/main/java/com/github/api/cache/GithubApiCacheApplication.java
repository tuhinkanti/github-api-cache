package com.github.api.cache;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class GithubApiCacheApplication {

	public static void main(String[] args) {
		SpringApplication.run(GithubApiCacheApplication.class, args);
	}

}
