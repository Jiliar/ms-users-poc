package bizz.addonai.users.msuserspoc.models.enums;

public enum SubscriptionType {
    FREE("Free"),
    BASIC("Basic"),
    PREMIUM(""),
    ENTERPRISE("Enterprise");

    private final String displayName;

    SubscriptionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
