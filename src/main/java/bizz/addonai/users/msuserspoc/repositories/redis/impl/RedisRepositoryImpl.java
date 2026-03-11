package bizz.addonai.users.msuserspoc.repositories.redis.impl;


import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import bizz.addonai.users.msuserspoc.repositories.redis.IRedisRepository;

import java.util.Optional;
import java.util.function.Supplier;

@Service
public class RedisRepositoryImpl implements IRedisRepository {

    private final CacheManager cacheManager;

    public RedisRepositoryImpl(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public <T> Optional<T> get(String cacheName, Object key, Class<T> type) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) return Optional.empty();
        
        Cache.ValueWrapper wrapper = cache.get(key);
        if (wrapper == null) return Optional.empty();
        
        Object value = wrapper.get();
        if (value == null) return Optional.empty();
        
        return Optional.of(type.cast(value));
    }

    @Override
    public <T> T getOrLoad(String cacheName, Object key, Class<T> type, Supplier<T> loader) {
        return get(cacheName, key, type).orElseGet(() -> {
            T value = loader.get();
            if (value != null) {
                put(cacheName, key, value);
            }
            return value;
        });
    }

    @Override
    public void put(String cacheName, Object key, Object value) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.put(key, value);
        }
    }

    @Override
    public void evict(String cacheName, Object key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
        }
    }

    @Override
    public void evictAll(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }

    @Override
    public boolean exists(String cacheName, Object key) {
        return get(cacheName, key, Object.class).isPresent();
    }
}