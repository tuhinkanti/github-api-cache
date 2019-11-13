package com.github.api.cache.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

public interface CacheService {

    WebClient getWebClient();
    void populateCache();
    Cache<String, JsonNode> getCache();
    Mono<JsonNode> getResponseFromUrl(String url);
    List<List<Object>> getTopNReposByForksFromCache(int n);
    List<List<Object>> getTopNReposByLastUpdatedFromCache(int n);
    List<List<Object>> getTopNReposByOpenIssuesFromCache(int n);
    List<List<Object>> getTopNReposByStarsFromCache(int n);
}
