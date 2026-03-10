package bizz.addonai.users.msuserspoc.dtos;

import bizz.addonai.users.msuserspoc.models.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFilterInput {

    /** ISO-8601 date (yyyy-MM-dd). Filters users created on or after this date. */
    private String startDate;

    /** ISO-8601 date (yyyy-MM-dd). Filters users created on or before this date. */
    private String endDate;

    private UserType userType;
}
