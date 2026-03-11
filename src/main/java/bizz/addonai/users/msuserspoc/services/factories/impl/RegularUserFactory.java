package bizz.addonai.users.msuserspoc.services.factories.impl;

import bizz.addonai.users.msuserspoc.dtos.CreateUserRequest;
import bizz.addonai.users.msuserspoc.models.RegularUser;
import bizz.addonai.users.msuserspoc.models.UserEntity;
import bizz.addonai.users.msuserspoc.models.enums.SubscriptionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RegularUserFactory implements UserFactory {

    @Override
    public UserEntity createUser(CreateUserRequest request, String encryptedPassword) {
        log.debug("[RegularUserFactory] createUser invocado - username={}, email={}, subscriptionType={}, newsletter={}",
                request.getUsername(), request.getEmail(), request.getSubscriptionType(), request.getNewsletterSubscribed());
        validateUserData(request);

        SubscriptionType subscription = request.getSubscriptionType() != null ? request.getSubscriptionType() : SubscriptionType.FREE;
        boolean newsletter = Boolean.TRUE.equals(request.getNewsletterSubscribed());

        RegularUser user = RegularUser.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(encryptedPassword)
                .subscriptionType(subscription)
                .newsletterSubscribed(newsletter)
                .build();

        log.debug("[RegularUserFactory] RegularUser construido - subscriptionType={}, newsletterSubscribed={}",
                user.getSubscriptionType(), user.isNewsletterSubscribed());
        return user;
    }

    @Override
    public String getFactoryType() {
        return "REGULAR";
    }

    @Override
    public void validateUserData(CreateUserRequest request) {
        log.debug("[RegularUserFactory] validateUserData - subscriptionType={}", request.getSubscriptionType());
        // subscriptionType is already type-safe via SubscriptionType enum
    }
}
