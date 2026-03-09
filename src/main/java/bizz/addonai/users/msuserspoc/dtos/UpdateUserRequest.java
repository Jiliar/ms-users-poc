package bizz.addonai.users.msuserspoc.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username can only contain letters, numbers, dots, hyphens and underscores")
    private String username;

    @Email(message = "Email must be a valid address")
    private String email;

    @Size(max = 50, message = "Admin level must not exceed 50 characters")
    private String adminLevel;

    @Size(max = 100, message = "Department must not exceed 100 characters")
    private String department;

    @Pattern(
            regexp = "^(FREE|BASIC|PREMIUM|ENTERPRISE)$",
            message = "Subscription type must be FREE, BASIC, PREMIUM or ENTERPRISE"
    )
    private String subscriptionType;

    private Boolean newsletterSubscribed;
}
