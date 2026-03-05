package bizz.addonai.users.msuserspoc.models;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("ADMIN")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUser extends UserEntity {

    private String adminLevel;
    private String department;

    @Override
    public String getPermissions() {
        return "ALL:READ,WRITE,DELETE,MANAGE_USERS,MANAGE_SYSTEM";
    }

    @Override
    public String getDashboardUrl() {
        return "/admin/dashboard";
    }
}