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

    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static boolean isNotEmpty(CharSequence cs) {
        return !isEmpty(cs);
    }

    public static boolean isBlank(CharSequence cs) {
        if (cs == null) {
            return true;
        }
        int length = cs.length();
        for (int i = 0; i < length; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNotBlank(CharSequence cs) {
        return !isBlank(cs);
    }

    /**
     * 判断字符串是否仅由0-9组成
     *
     * @param str
     * @return
     */
    public static boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) < '0' || str.charAt(i) > '9') {
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

    public static List<String> split(String str) {
        return split(str, ",", true);
    }

    public static List<String> split(String str, String delimiter, boolean trim) {
        if (str == null) {
            return new ArrayList<>();
        }

        if (delimiter == null) {
            throw new IllegalArgumentException();
        }
        String[] tokens = str.split(delimiter, -1);
        Stream<String> tokensStream = Arrays.asList(tokens).stream();
        if (trim) {
            tokensStream = tokensStream.map(String::trim).filter(s -> !s.isEmpty());
        }
        return tokensStream.collect(Collectors.toList());
    }

    /**
     * 统计字符出现的总数
     *
     * @param str
     * @param c
     * @return
     */
    public static int countChar(String str, char c) {
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == c) {
                count++;
            }
        }
        return count;
    }

}
