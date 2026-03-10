package bizz.addonai.users.msuserspoc.models;

import bizz.addonai.users.msuserspoc.models.enums.SubscriptionType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("REGULAR")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RegularUser extends UserEntity {

    @Enumerated(EnumType.STRING)
    private SubscriptionType subscriptionType;
    private boolean newsletterSubscribed;

    @Override
    public String getPermissions() {
        return "LIMITED:READ,WRITE";
    }

    @Override
    public String getDashboardUrl() {
        return "/user/dashboard";
    }

    public SubscriptionType getSubscriptionType() {
        return subscriptionType;
    }

    public boolean isNewsletterSubscribed() {
        return newsletterSubscribed;
    }
}
