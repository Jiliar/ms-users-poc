package bizz.addonai.users.msuserspoc.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.lang.NonNull;

@Slf4j
public class CustomCacheErrorHandler implements CacheErrorHandler {

    @Override
    public void handleCacheGetError(@NonNull RuntimeException exception, @NonNull Cache cache, @NonNull Object key) {
        log.warn("[Resiliencia Caché] Fallo de lectura en Redis (caché: '{}', llave: '{}'). Fallback automático a Base de Datos. Error: {}", 
                 cache.getName(), key, exception.getMessage());
    }

    @Override
    public void handleCachePutError(@NonNull RuntimeException exception, @NonNull Cache cache, @NonNull Object key, Object value) {
        log.warn("[Resiliencia Caché] Fallo de escritura en Redis (caché: '{}', llave: '{}'). El dato se guardó en BD, pero no en caché. Error: {}", 
                 cache.getName(), key, exception.getMessage());
    }

    @Override
    public void handleCacheEvictError(@NonNull RuntimeException exception, @NonNull Cache cache, @NonNull Object key) {
        log.warn("[Resiliencia Caché] Fallo al invalidar llave en Redis (caché: '{}', llave: '{}'). Error: {}", 
                 cache.getName(), key, exception.getMessage());
    }

    @Override
    public void handleCacheClearError(@NonNull RuntimeException exception, @NonNull Cache cache) {
        log.warn("[Resiliencia Caché] Fallo al limpiar completamente la caché '{}'. Error: {}", 
                 cache.getName(), exception.getMessage());
    }
}