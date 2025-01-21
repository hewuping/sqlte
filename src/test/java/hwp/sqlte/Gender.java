package hwp.sqlte;

public enum Gender {
    MALE("男"),
    FEMALE("女"),
    OTHER("其他");

    private final String displayName;

    Gender(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}