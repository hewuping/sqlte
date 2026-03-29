package hwp.sqlte.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link NameUtils} – camelCase / underscore name conversions.
 */
public class NameUtilsTest {

    // ─── toUnderscore ────────────────────────────────────────────────────────

    @ParameterizedTest
    @CsvSource({
        "upperCamel,    upper_camel",
        "UpperCamel,    upper_camel",
        "UpperCamelAbc, upper_camel_abc",
        "id,            id",
        "ID,            i_d",
        "userName,      user_name",
        "a,             a",
        "A,             a"
    })
    void toUnderscore(String input, String expected) {
        assertEquals(expected.trim(), NameUtils.toUnderscore(input.trim()));
    }

    // ─── toUpperCamel ────────────────────────────────────────────────────────

    @ParameterizedTest
    @CsvSource({
        "upper_camel,     upperCamel",
        "_upper_camel_,   UpperCamel",
        "upper_camel_abc, upperCamelAbc",
        "UPPER_CAMEL_ABC, upperCamelAbc",
        "a,               a",
        "a_b,             aB"
    })
    void toUpperCamel(String input, String expected) {
        assertEquals(expected.trim(), NameUtils.toUpperCamel(input.trim()));
    }

    // ─── toClassName ─────────────────────────────────────────────────────────

    @ParameterizedTest
    @CsvSource({
        "user_profile,   UserProfile",
        "order,          Order",
        "order_item_log, OrderItemLog",
        "a,              A",
        "a_b_c,          ABC"
    })
    void toClassName(String input, String expected) {
        assertEquals(expected.trim(), NameUtils.toClassName(input.trim()));
    }

    // ─── upperFisrt ──────────────────────────────────────────────────────────

    @Test
    void upperFisrt_alreadyUpper() {
        assertEquals("Hello", NameUtils.upperFisrt("Hello"));
    }

    @Test
    void upperFisrt_lower() {
        assertEquals("Hello", NameUtils.upperFisrt("hello"));
    }

    @Test
    void upperFisrt_singleChar() {
        assertEquals("A", NameUtils.upperFisrt("a"));
    }

    @Test
    void upperFisrt_singleUpper() {
        assertEquals("A", NameUtils.upperFisrt("A"));
    }

    // ─── round-trip consistency ───────────────────────────────────────────────

    @ParameterizedTest
    @CsvSource({
        "userName",
        "orderItemCount",
        "firstName"
    })
    void toUnderscore_thenToUpperCamel_roundtrip(String camel) {
        String underscore = NameUtils.toUnderscore(camel);
        String back = NameUtils.toUpperCamel(underscore);
        assertEquals(camel, back, "Round-trip failed for: " + camel);
    }
}
