package bizz.addonai.users.msuserspoc.services.factories.impl;

import bizz.addonai.users.msuserspoc.dtos.CreateUserRequest;
import bizz.addonai.users.msuserspoc.models.RegularUser;
import bizz.addonai.users.msuserspoc.models.UserEntity;
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
                .subscriptionType(request.getSubscriptionType() != null ? request.getSubscriptionType() : "FREE")
                .newsletterSubscribed(request.getNewsletterSubscribed() != null ? request.getNewsletterSubscribed() : false)
                .build();
    }

    @Override
    public String getFactoryType() {
        return "REGULAR";
    }

    @Override
    public void validateUserData(CreateUserRequest request) {
        if (request.getSubscriptionType() != null) {
            String type = request.getSubscriptionType().toUpperCase();
            if (!type.matches("FREE|BASIC|PREMIUM|ENTERPRISE")) {
                throw new IllegalArgumentException("Invalid subscription type: " + type);
            }
        }
    }
}