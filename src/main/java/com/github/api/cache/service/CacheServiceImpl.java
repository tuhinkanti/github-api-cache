package com.github.api.cache.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.github.api.cache.CacheServiceConstants;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.github.api.cache.CacheServiceConstants.NETFLIX_ORG_REPOS_URL;

@Service
public class CacheServiceImpl implements CacheService {

    private static final Logger logger = LoggerFactory.getLogger(CacheServiceImpl.class);
    private static final int CACHE_SIZE = 100;

    private String authorizationToken;

    private String githubApiBaseUrl;
    private WebClient webClient;
    private Cache<String, JsonNode> cache;

    public CacheServiceImpl(@Value("${github.api.base-url}") String githubApiBaseUrl,
                            @Value("${github.api.token}") String authorizationToken) {
        this.githubApiBaseUrl = githubApiBaseUrl;
        this.authorizationToken = authorizationToken;
        this.webClient = buildWebClient();
        this.cache = buildCache();
    }

    @Override
    public WebClient getWebClient() {
        return this.webClient;
    }

    public Cache<String, JsonNode> getCache() {
        return this.cache;
    }

    public JsonNode getValueFromCache(String key) {
        populateCache();
        return getCache().getIfPresent(key);
    }

    private WebClient buildWebClient() {
        return WebClient.builder()
                .baseUrl(githubApiBaseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/vnd.github.v3+json")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "token " + authorizationToken)
                .build();
    }

    private LoadingCache<String, JsonNode> buildCache() {
        return Caffeine.newBuilder()
                .maximumSize(CACHE_SIZE)
                .refreshAfterWrite(1, TimeUnit.MINUTES)
                .build(key -> getValueFromCache(key));
    }

    public void populateCache() {
        getResponseFromUrl(CacheServiceConstants.BASE_URL).subscribe();
        getResponseFromUrl(CacheServiceConstants.NETFLIX_ORG_URL).subscribe();
        getResponseFromUrl(CacheServiceConstants.NETFLIX_ORG_MEMBERS_URL).subscribe();
        getResponseFromUrl(CacheServiceConstants.NETFLIX_ORG_REPOS_URL).subscribe();
    }

    private Mono<ClientResponse> getRestResponseFromUriString(String uriString) {
        return webClient
                .get()
                .uri(uriString)
                .exchange()
                .retryBackoff(1, Duration.ofMillis(500L));
    }

    @Override
    public Mono<JsonNode> getResponseFromUrl(String url) {
        JsonNode cachedVersion = getCache().getIfPresent(url);
        if (cachedVersion != null) {
            return Mono.just(cachedVersion);
        } else {
            Mono<ClientResponse> response = getRestResponseFromUriString(url)
                    .switchIfEmpty(Mono.error(new Exception()));

            return response.flatMap(result -> {
                        try {
                            Mono<ResponseEntity<JsonNode>> responseEntityMono = result.toEntity(JsonNode.class);
                            return responseEntityMono.map(jsonNodeResponseEntity ->
                                    (jsonNodeResponseEntity != null &&
                                            jsonNodeResponseEntity.hasBody()) ?
                                            jsonNodeResponseEntity.getBody() : NullNode.getInstance());
                        } catch (Throwable t) {
                            throw Exceptions.propagate(t);
                        }
                    }
            ).doOnNext(r -> this.getCache().put(url, r));
        }
    }

    @Override
    public List<List<Object>> getTopNReposByForksFromCache(int n) {
        List<List<Object>> viewsList = new ArrayList<>();
        JsonNode cachedJsonValue = getValueFromCache(NETFLIX_ORG_REPOS_URL);
        ArrayNode jsonNodesArray = (ArrayNode) cachedJsonValue;
        Iterator<JsonNode> forksIterator = jsonNodesArray.elements();
        while(forksIterator.hasNext()) {
            JsonNode jsonNode = forksIterator.next();
            List<Object> element = new ArrayList<>();
            element.add(jsonNode.get("full_name").asText());
            element.add(jsonNode.get("forks").asInt());
            viewsList.add(element);
        }

        viewsList = viewsList.stream()
                .limit(n)
                .sorted((f1,f2) -> Integer.parseInt(f2.get(1).toString()) - Integer.parseInt(f1.get(1).toString()))
                .collect(Collectors.toList());
        return viewsList;
    }

    @Override
    public List<List<Object>> getTopNReposByLastUpdatedFromCache(int n) {
        List<List<Object>> viewsList = new ArrayList<>();
        JsonNode cachedJsonValue = getValueFromCache(NETFLIX_ORG_REPOS_URL);
        ArrayNode jsonNodesArray = (ArrayNode) cachedJsonValue;
        Iterator<JsonNode> forksIterator = jsonNodesArray.elements();
        while(forksIterator.hasNext()) {
            JsonNode jsonNode = forksIterator.next();
            List<Object> element = new ArrayList<>();
            element.add(jsonNode.get("full_name").asText());
            element.add(jsonNode.get("updated_at").asText());
            viewsList.add(element);
        }

        viewsList = viewsList.stream()
                .limit(n)
                .sorted((f1,f2) -> Instant.parse(f2.get(1).toString()).compareTo(Instant.parse(f1.get(1).toString())))
                .collect(Collectors.toList());
        return viewsList;
    }

    @Override
    public List<List<Object>> getTopNReposByOpenIssuesFromCache(int n) {
        List<List<Object>> viewsList = new ArrayList<>();
        JsonNode cachedJsonValue = getValueFromCache(NETFLIX_ORG_REPOS_URL);
        ArrayNode jsonNodesArray = (ArrayNode) cachedJsonValue;
        Iterator<JsonNode> forksIterator = jsonNodesArray.elements();
        while(forksIterator.hasNext()) {
            JsonNode jsonNode = forksIterator.next();
            List<Object> element = new ArrayList<>();
            element.add(jsonNode.get("full_name").asText());
            element.add(jsonNode.get("open_issues_count").asInt());
            viewsList.add(element);
        }

        viewsList = viewsList.stream()
                .limit(n)
                .sorted((f1,f2) -> Integer.parseInt(f2.get(1).toString()) - Integer.parseInt(f1.get(1).toString()))
                .collect(Collectors.toList());
        return viewsList;
    }

    @Override
    public List<List<Object>> getTopNReposByStarsFromCache(int n) {
        List<List<Object>> viewsList = new ArrayList<>();
        JsonNode cachedJsonValue = getValueFromCache(NETFLIX_ORG_REPOS_URL);
        ArrayNode jsonNodesArray = (ArrayNode) cachedJsonValue;
        Iterator<JsonNode> forksIterator = jsonNodesArray.elements();
        while(forksIterator.hasNext()) {
            JsonNode jsonNode = forksIterator.next();
            List<Object> element = new ArrayList<>();
            element.add(jsonNode.get("full_name").asText());
            element.add(jsonNode.get("stargazers_count").asInt());
            viewsList.add(element);
        }

        viewsList = viewsList.stream()
                .limit(n)
                .sorted((f1,f2) -> Integer.parseInt(f2.get(1).toString()) - Integer.parseInt(f1.get(1).toString()))
                .collect(Collectors.toList());
        return viewsList;
    }
}
