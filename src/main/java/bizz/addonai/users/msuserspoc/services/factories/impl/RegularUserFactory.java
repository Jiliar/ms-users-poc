package bizz.addonai.users.msuserspoc.services.factories.impl;

import bizz.addonai.users.msuserspoc.dtos.CreateUserRequest;
import bizz.addonai.users.msuserspoc.models.RegularUser;
import bizz.addonai.users.msuserspoc.models.UserEntity;
import bizz.addonai.users.msuserspoc.models.enums.SubscriptionType;
import org.springframework.stereotype.Component;

@Component
public class RegularUserFactory implements UserFactory {

    @Override
    public UserEntity createUser(CreateUserRequest request, String encryptedPassword) {
        validateUserData(request);

        return RegularUser.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(encryptedPassword)
                .subscriptionType(request.getSubscriptionType() != null ? request.getSubscriptionType() : SubscriptionType.FREE)
                .newsletterSubscribed(request.getNewsletterSubscribed() != null ? request.getNewsletterSubscribed() : false)
                .build();
    }

    @Override
    public String getFactoryType() {
        return "REGULAR";
    }

    @Override
    public void validateUserData(CreateUserRequest request) {
        // subscriptionType is already type-safe via SubscriptionType enum
    }
}
