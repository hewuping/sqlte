package hwp.sqlte.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link StringUtils}.
 */
public class StringUtilsTest {

    // ─── isEmpty / isNotEmpty ────────────────────────────────────────────────

    @Test
    void isEmpty_null() {
        assertTrue(StringUtils.isEmpty(null));
    }

    @Test
    void isEmpty_emptyString() {
        assertTrue(StringUtils.isEmpty(""));
    }

    @Test
    void isEmpty_nonEmpty() {
        assertFalse(StringUtils.isEmpty("a"));
    }

    @Test
    void isNotEmpty_null() {
        assertFalse(StringUtils.isNotEmpty(null));
    }

    @Test
    void isNotEmpty_value() {
        assertTrue(StringUtils.isNotEmpty("x"));
    }

    // ─── isBlank / isNotBlank ────────────────────────────────────────────────

    @Test
    void isBlank_null() {
        assertTrue(StringUtils.isBlank(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "  ", "\t", "\n", " \t\n"})
    void isBlank_whitespace(String input) {
        assertTrue(StringUtils.isBlank(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", " a", "a ", " a "})
    void isBlank_nonBlank(String input) {
        assertFalse(StringUtils.isBlank(input));
    }

    @Test
    void isNotBlank_null() {
        assertFalse(StringUtils.isNotBlank(null));
    }

    @Test
    void isNotBlank_value() {
        assertTrue(StringUtils.isNotBlank("hello"));
    }

    // ─── isNumeric ───────────────────────────────────────────────────────────

    @Test
    void isNumeric_null() {
        assertFalse(StringUtils.isNumeric(null));
    }

    @Test
    void isNumeric_empty() {
        assertFalse(StringUtils.isNumeric(""));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "1", "123", "9999"})
    void isNumeric_valid(String input) {
        assertTrue(StringUtils.isNumeric(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1a", "abc", "-1", "1.0", " 1"})
    void isNumeric_invalid(String input) {
        assertFalse(StringUtils.isNumeric(input));
    }

    // ─── split ───────────────────────────────────────────────────────────────

    @Test
    void split_null() {
        List<String> result = StringUtils.split(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void split_commaSeparated() {
        List<String> result = StringUtils.split("a, b, c");
        assertEquals(List.of("a", "b", "c"), result);
    }

    @Test
    void split_trimming() {
        List<String> result = StringUtils.split("  x ,  y  ");
        assertEquals(List.of("x", "y"), result);
    }

    @Test
    void split_skipsEmptyTokens() {
        List<String> result = StringUtils.split("a,,b");
        assertEquals(List.of("a", "b"), result);
    }

    @Test
    void splitToArray_basic() {
        String[] arr = StringUtils.splitToArray("one,two,three");
        assertArrayEquals(new String[]{"one", "two", "three"}, arr);
    }

    // ─── countChar ───────────────────────────────────────────────────────────

    @Test
    void countChar_zero() {
        assertEquals(0, StringUtils.countChar("hello", 'z'));
    }

    @Test
    void countChar_single() {
        assertEquals(1, StringUtils.countChar("hello", 'e'));
    }

    @Test
    void countChar_multiple() {
        assertEquals(3, StringUtils.countChar("banana", 'a'));
    }

    @Test
    void countChar_allMatch() {
        assertEquals(4, StringUtils.countChar("aaaa", 'a'));
    }
}
