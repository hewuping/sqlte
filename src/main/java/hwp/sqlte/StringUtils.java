package hwp.sqlte;

/**
 * @author Zero
 * Created on 2021/2/9.
 */
class StringUtils {
    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static boolean isNotEmpty(final CharSequence cs) {
        return !isEmpty(cs);
    }
}
