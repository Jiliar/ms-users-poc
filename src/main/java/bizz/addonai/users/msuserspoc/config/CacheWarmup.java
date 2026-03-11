package bizz.addonai.users.msuserspoc.config;

import bizz.addonai.users.msuserspoc.dtos.PageInput;
import bizz.addonai.users.msuserspoc.dtos.enums.SortDirection;
import bizz.addonai.users.msuserspoc.services.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CacheWarmup {

    private final IUserService userService;

    public CacheWarmup(IUserService userService) {
        this.userService = userService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void warmupCache() {
        log.info("Starting cache warmup...");
        try {
            var users = userService.getAllUsers(null, new PageInput(0, 100, "createdAt", SortDirection.ASC));
            log.info("Warmed up {} users in cache", users.getPageInfo().getTotalElements());
        } catch (Exception e) {
            log.warn("Cache warmup failed during application startup. Error: {}", e.getMessage());
        }
    }
}