package bizz.addonai.users.msuserspoc.dtos;

import bizz.addonai.users.msuserspoc.dtos.enums.SortDirection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageInput {

    /** Zero-based page index. Defaults to 0. */
    private Integer page;

    /** Number of items per page (1–100). Defaults to 10. */
    private Integer size;

    /** Field to sort by. Allowed: createdAt, updatedAt, username, email. Defaults to createdAt. */
    private String sortBy;

    /** ASC or DESC. Defaults to DESC. */
    private SortDirection sortDirection;
}
