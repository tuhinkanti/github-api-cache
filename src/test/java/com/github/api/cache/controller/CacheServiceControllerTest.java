package com.github.api.cache.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.api.cache.service.CacheService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Mockito.when;

@WebFluxTest(CacheServiceController.class)
public class CacheServiceControllerTest {

    @Autowired
    public WebTestClient webTestClient;

    @MockBean
    public CacheService cacheService;

    @Test
    public void testGetEmployeeById() throws JsonProcessingException {
        String jsonString = "{\"login\": \"Netflix\",\"id\": \"913567\",\"node_id\": \"MDEyOk9yZ2FuaXphdGlvbjkxMzU2Nw==\"}";

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonObj = mapper.readTree(jsonString);
        Mono<JsonNode> orgsMono = Mono.just(jsonObj);
        when(cacheService.getResponseFromUrl("/orgs/Netflix")).thenReturn(orgsMono);

        webTestClient.get()
                .uri("/orgs/Netflix")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(JsonNode.class)
                .value(j -> jsonObj.get("id").asText(), equalTo("913567"));
    }

}
