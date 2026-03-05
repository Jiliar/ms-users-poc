package bizz.addonai.users.msuserspoc.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    private String username;
    private String email;
    private String adminLevel;
    private String department;
    private String subscriptionType;
    private Boolean newsletterSubscribed;
}
