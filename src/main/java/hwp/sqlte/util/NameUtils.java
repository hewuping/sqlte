package hwp.sqlte.util;

public class NameUtils {

    /**
     * <pre>{@code
     *  upperCamel    -> upper_camel;
     *  UpperCamel    -> upper_camel;
     *  UpperCamelAbc -> upper_camel_abc;
     * } </pre>
     *
     * @param upperCamel
     * @return
     */
    public static String toUnderscore(String upperCamel) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0, len = upperCamel.length(); i < len; i++) {
            char c = upperCamel.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    builder.append('_');
                }
                builder.append(Character.toLowerCase(c));
            } else {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    /**
     * <pre>{@code
     *  upper_camel     ->  upperCamel;
     *  _upper_camel_   ->  upperCamel;
     *  upper_camel_abc ->  upperCamelAbc;
     *  UPPER_CAMEL_ABC ->  upperCamelAbc;
     * } </pre>
     *
     * @param lower_underscore
     * @return
     */
    public static String toUpperCamel(String lower_underscore) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0, len = lower_underscore.length(); i < len; i++) {
            char c = lower_underscore.charAt(i);
            if ('_' == c || '-' == c) {
                i++;
                if (i >= len) {
                    break;
                }
                char next = lower_underscore.charAt(i);
                builder.append(Character.toUpperCase(next));
            } else {
                builder.append(Character.toLowerCase(c));
            }
        }
        return builder.toString();
    }

    public static String toClassName(String lower_underscore) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0, len = lower_underscore.length(); i < len; i++) {
            char c = lower_underscore.charAt(i);
            if (i == 0) {
                builder.append(Character.toUpperCase(c));
                continue;
            }
            if ('_' == c || '-' == c) {
                i++;
                if (i >= len) {
                    break;
                }
                char next = lower_underscore.charAt(i);
                builder.append(Character.toUpperCase(next));
                continue;
            }
            builder.append(Character.toLowerCase(c));
        }
        return builder.toString();
    }

    public static void main(String[] args) {
        System.out.println(toUnderscore("upperCamel"));
        System.out.println(toUnderscore("UpperCamel"));
        System.out.println(toUnderscore("UpperCamelAbc"));
        System.out.println(toUpperCamel("upper_camel"));
        System.out.println(toUpperCamel("_upper_camel_"));
        System.out.println(toUpperCamel("upper_camel_abc"));
        System.out.println(toUpperCamel("UPPER_CAMEL_ABC"));


    }

}
