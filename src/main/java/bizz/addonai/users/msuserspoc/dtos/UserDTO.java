package bizz.addonai.users.msuserspoc.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import bizz.addonai.users.msuserspoc.models.enums.SubscriptionType;
import bizz.addonai.users.msuserspoc.models.enums.UserType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
public class UserDTO implements Serializable{

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
