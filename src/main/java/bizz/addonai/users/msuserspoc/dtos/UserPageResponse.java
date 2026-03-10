package bizz.addonai.users.msuserspoc.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPageResponse {

    private List<UserDTO> content;
    private PageMetadata pageInfo;
}
