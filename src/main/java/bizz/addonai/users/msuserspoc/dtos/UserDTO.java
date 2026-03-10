package bizz.addonai.users.msuserspoc.dtos;

import bizz.addonai.users.msuserspoc.models.enums.SubscriptionType;
import bizz.addonai.users.msuserspoc.models.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO implements UserResult{
    private UUID id;
    private String username;
    private String email;
    private UserType userType;
    private String permissions;
    private String dashboardUrl;
    private LocalDateTime createdAt;
    private String adminLevel;
    private String department;
    private SubscriptionType subscriptionType;
    private Boolean newsletterSubscribed;
}
