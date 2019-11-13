package com.github.api.cache.config;

import com.github.api.cache.service.CacheService;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class CacheConfig implements
        ApplicationListener<ContextRefreshedEvent> {

    CacheService cacheService;

    CacheConfig (CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        cacheService.populateCache();
    }
}
