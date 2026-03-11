package bizz.addonai.users.msuserspoc.models.enums;

public enum UserType {
    ADMIN("Admin"),
    REGULAR("Regular");

    private final String displayName;

    UserType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
