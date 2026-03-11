package bizz.addonai.users.msuserspoc.dtos;

import bizz.addonai.users.msuserspoc.models.enums.UserType;
import bizz.addonai.users.msuserspoc.models.enums.SubscriptionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO{
    private UUID id;
    private String username;
    private String email;
    private UserType userType;
    private String permissions;
    private String dashboardUrl;
    private String createdAt;
    private String adminLevel;
    private String department;
    private SubscriptionType subscriptionType;
    private Boolean newsletterSubscribed;
}
