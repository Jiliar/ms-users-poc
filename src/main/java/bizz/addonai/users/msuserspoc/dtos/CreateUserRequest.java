package bizz.addonai.users.msuserspoc.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
public class CreateUserRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username can only contain letters, numbers, dots, hyphens and underscores")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
            message = "Password must contain at least 1 uppercase, 1 lowercase, 1 number and 1 special character (@$!%*?&)"
    )
    private String password;

    @NotBlank(message = "User type is required")
    @Pattern(regexp = "^(ADMIN|REGULAR)$", message = "User type must be ADMIN or REGULAR")
    private String userType;

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
