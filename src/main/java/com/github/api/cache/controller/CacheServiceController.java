package com.github.api.cache.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.github.api.cache.CacheServiceConstants;
import com.github.api.cache.service.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

import static com.github.api.cache.CacheServiceConstants.*;

@RestController
public class CacheServiceController {

    private static final Logger logger = LoggerFactory.getLogger(CacheServiceController.class);

    CacheService cacheService;

    CacheServiceController(CacheService cacheService) {

        this.cacheService = cacheService;
    }

    @GetMapping(BASE_URL)
    public Mono<JsonNode> getGitHubApiDetails() {
        return cacheService.getResponseFromUrl(BASE_URL);
    }

    @GetMapping(NETFLIX_ORG_URL)
    public Mono<JsonNode> getGithubNetflixOrgDetails() {
        return cacheService.getResponseFromUrl(NETFLIX_ORG_URL);
    }

    @GetMapping(NETFLIX_ORG_MEMBERS_URL)
    public Mono<JsonNode> getGithubNetflixOrgMembers() {
        return cacheService.getResponseFromUrl(NETFLIX_ORG_MEMBERS_URL);
    }

    @GetMapping(NETFLIX_ORG_REPOS_URL)
    public Mono<JsonNode> getGithubNetflixOrgRepos() {
        return cacheService.getResponseFromUrl(CacheServiceConstants.NETFLIX_ORG_REPOS_URL);
    }

    @GetMapping(NETFLIX_ORG_REPOS_TOP_N_FORKS_URL)
    public List<List<Object>> getGithubNetflixOrgTopNReposByForks(@PathVariable("N") int N) {
        return cacheService.getTopNReposByForksFromCache(N);
    }

    @GetMapping(NETFLIX_ORG_REPOS_TOP_N_LAST_UPDATED_URL)
    public List<List<Object>> getGithubNetflixOrgTopNReposByLastUpdated(@PathVariable("N") int N) {
        return cacheService.getTopNReposByLastUpdatedFromCache(N);
    }

    @GetMapping(NETFLIX_ORG_REPOS_TOP_N_OPEN_ISSUES_URL)
    public List<List<Object>> getGithubNetflixOrgTopNReposByOpenIssues(@PathVariable("N") int N) {
        return cacheService.getTopNReposByOpenIssuesFromCache(N);
    }

    @GetMapping(NETFLIX_ORG_REPOS_TOP_N_STARS_URL)
    public List<List<Object>> getGithubNetflixOrgTopNReposByStars(@PathVariable("N") int N) {
        return cacheService.getTopNReposByStarsFromCache(N);
    }

    @GetMapping(OTHER_URL)
    public Mono<JsonNode> getGithubAllOthers(ServerHttpRequest request) {
        String uriString = request.getPath().toString();
        Mono<ClientResponse> response = cacheService.getWebClient()
                .get()
                .uri(uriString)
                .exchange()
                .retryBackoff(1, Duration.ofMillis(500L))
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
        });
    }
}
