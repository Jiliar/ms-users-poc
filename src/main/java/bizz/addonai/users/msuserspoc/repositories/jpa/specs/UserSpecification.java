package bizz.addonai.users.msuserspoc.repositories.jpa.specs;

import bizz.addonai.users.msuserspoc.dtos.UserFilterInput;
import bizz.addonai.users.msuserspoc.models.UserEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    private static final String CREATED_AT = "createdAt";
    private static final String USER_TYPE   = "userType";

    private UserSpecification() {}

    public static Specification<UserEntity> withFilters(UserFilterInput filter) {
        return (root, query, cb) -> {
            if (filter == null) return cb.conjunction();

            List<Predicate> predicates = new ArrayList<>();

            if (filter.getStartDate() != null) {
                LocalDateTime start = parseStartOfDay(filter.getStartDate());
                predicates.add(cb.greaterThanOrEqualTo(root.get(CREATED_AT), start));
            }

            if (filter.getEndDate() != null) {
                LocalDateTime end = parseEndOfDay(filter.getEndDate());
                predicates.add(cb.lessThanOrEqualTo(root.get(CREATED_AT), end));
            }

            if (filter.getUserType() != null) {
                predicates.add(cb.equal(root.get(USER_TYPE), filter.getUserType()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static LocalDateTime parseStartOfDay(String date) {
        try {
            return LocalDate.parse(date).atStartOfDay();
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format for startDate: '" + date + "'. Expected yyyy-MM-dd");
        }
    }

    private static LocalDateTime parseEndOfDay(String date) {
        try {
            return LocalDate.parse(date).atTime(23, 59, 59, 999_999_999);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format for endDate: '" + date + "'. Expected yyyy-MM-dd");
        }
    }
}
