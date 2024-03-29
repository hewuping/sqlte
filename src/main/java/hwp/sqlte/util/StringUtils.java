package hwp.sqlte.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Zero
 * Created on 2021/2/9.
 */
public class StringUtils {
    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static boolean isNotEmpty(final CharSequence cs) {
        return !isEmpty(cs);
    }

    public static boolean isBlank(final String cs) {
        return cs == null || cs.trim().isEmpty();
    }

    public static boolean isNotBlank(final String cs) {
        return !isBlank(cs);
    }

    public static boolean isNumber(String s) {
        if (s.length() == 0) {
            return false;
        }
        for (char c : s.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    @Deprecated
    public static String toUnderscore(String upperCamel) {
        return NameUtils.toUnderscore(upperCamel);
    }

    public static String[] splitToArray(String str) {
        return split(str, ",", true).toArray(new String[0]);
    }

    public static List<String> split(String stringToSplit) {
        return split(stringToSplit, ",", true);
    }

    public static List<String> split(String stringToSplit, String delimiter, boolean trim) {
        if (stringToSplit == null) {
            return new ArrayList<>();
        }

        if (delimiter == null) {
            throw new IllegalArgumentException();
        }
        String[] tokens = stringToSplit.split(delimiter, -1);
        Stream<String> tokensStream = Arrays.asList(tokens).stream();
        if (trim) {
            tokensStream = tokensStream.map(String::trim).filter(s -> s.length() > 0);
        }
        return tokensStream.collect(Collectors.toList());
    }

}
