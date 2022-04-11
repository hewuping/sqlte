package hwp.sqlte;

/**
 * @author Zero
 * Created on 2022/4/8.
 */
public enum Direction {
    ASC, DESC;

    public static Direction find(String str) {
        for (Direction value : values()) {
            if (value.name().equalsIgnoreCase(str)) {
                return value;
            }
        }
        return null;
    }

}
