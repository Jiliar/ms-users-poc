package bizz.addonai.users.msuserspoc.models;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
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

    private String subscriptionType;
    private boolean newsletterSubscribed;

    @Override
    public String getPermissions() {
        return "LIMITED:READ,WRITE";
    }

    @Override
    public String getDashboardUrl() {
        return "/user/dashboard";
    }

    public String getSubscriptionType() {
        return subscriptionType;
    }

    public boolean isNewsletterSubscribed() {
        return newsletterSubscribed;
    }
}
