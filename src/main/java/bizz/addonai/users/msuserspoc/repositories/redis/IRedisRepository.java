package bizz.addonai.users.msuserspoc.repositories.redis;


import java.util.Optional;
import java.util.function.Supplier;


public interface IRedisRepository {
    
    <T> Optional<T> get(String cacheName, Object key, Class<T> type);
    
    <T> T getOrLoad(String cacheName, Object key, Class<T> type, Supplier<T> loader);
    
    void put(String cacheName, Object key, Object value);
    
    void evict(String cacheName, Object key);
    
    void evictAll(String cacheName);
    
    boolean exists(String cacheName, Object key);
}
