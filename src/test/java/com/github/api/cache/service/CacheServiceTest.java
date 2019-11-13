package com.github.api.cache.service;

import com.fasterxml.jackson.databind.JsonNode;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CacheServiceTest {


    public static MockWebServer mockBackEnd;

    CacheService cacheService;

    @BeforeAll
    static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @BeforeEach
    void initialize() {
        String baseUrl = String.format("http://localhost:%s",
                mockBackEnd.getPort());
        cacheService = new CacheServiceImpl(baseUrl, "12345");
    }

    @Test
    void testGetResponseFromUrl() throws InterruptedException {
        String jsonString = "{\"current_user_url\": \"https://api.github.com/user\"," +
                "\"current_user_authorizations_html_url\": \"https://github.com/settings/connections/applications{/client_id}\"}";
        mockBackEnd.enqueue(new MockResponse()
                .setBody(jsonString)
                .addHeader("Content-Type", "application/json"));

        Mono<JsonNode> baseMono = cacheService.getResponseFromUrl("/");
        StepVerifier.create(baseMono)
                .expectNextMatches(r -> cacheService.getCache().estimatedSize() == 1)
                .verifyComplete();

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/", recordedRequest.getPath());
    }


    @Test
    void testGetTopNReposByForksFromCache() throws InterruptedException {
        String jsonString = "{\"current_user_url\": \"https://api.github.com/user\"," +
                "\"current_user_authorizations_html_url\": \"https://github.com/settings/connections/applications{/client_id}\"}";
        mockBackEnd.enqueue(new MockResponse()
                .setBody(jsonString)
                .addHeader("Content-Type", "application/json"));

        Mono<JsonNode> baseMono = cacheService.getResponseFromUrl("/");
        StepVerifier.create(baseMono)
                .expectNextMatches(r -> cacheService.getCache().estimatedSize() == 1)
                .verifyComplete();

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/", recordedRequest.getPath());
    }
}


