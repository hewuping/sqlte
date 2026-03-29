package hwp.sqlte;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link Helper} – SQL-generation utility methods.
 */
public class HelperTest {

    // ─── makeInsertSql ────────────────────────────────────────────────────────

    @Test
    void makeInsertSql_varargs() {
        String sql = Helper.makeInsertSql("users", new String[]{"username", "email", "password"});
        assertEquals("INSERT INTO users(username, email, password) VALUES (?, ?, ?)", sql);
    }

    @Test
    void makeInsertSql_commaString() {
        String sql = Helper.makeInsertSql("users", "username,email,password");
        assertEquals("INSERT INTO users(username, email, password) VALUES (?, ?, ?)", sql);
    }

    @Test
    void makeInsertSql_commaStringWithSpaces() {
        String sql = Helper.makeInsertSql("users", "username, email, password");
        assertEquals("INSERT INTO users(username, email, password) VALUES (?, ?, ?)", sql);
    }

    @Test
    void makeInsertSql_singleColumn() {
        String sql = Helper.makeInsertSql("logs", "message");
        assertEquals("INSERT INTO logs(message) VALUES (?)", sql);
    }

    @Test
    void makeInsertSql_customPrefix() {
        String sql = Helper.makeInsertSql("INSERT IGNORE INTO", "users", new String[]{"id", "name"});
        assertEquals("INSERT IGNORE INTO users(id, name) VALUES (?, ?)", sql);
    }

    @Test
    void makeInsertSql_nullPrefix_defaultsToInsertInto() {
        String sql = Helper.makeInsertSql(null, "users", new String[]{"id"});
        assertEquals("INSERT INTO users(id) VALUES (?)", sql);
    }

    @Test
    void makeInsertSql_emptyPrefix_defaultsToInsertInto() {
        String sql = Helper.makeInsertSql("", "users", new String[]{"id"});
        assertEquals("INSERT INTO users(id) VALUES (?)", sql);
    }

    // ─── makeUpdateSql ────────────────────────────────────────────────────────

    @Test
    void makeUpdateSql_withPk() {
        String sql = Helper.makeUpdateSql("users", new String[]{"username", "email"}, new String[]{"id"});
        assertEquals("UPDATE users SET username=?, email=? WHERE id =?", sql);
    }

    @Test
    void makeUpdateSql_withCompositePk() {
        String sql = Helper.makeUpdateSql("order_items",
                new String[]{"quantity", "price"},
                new String[]{"order_id", "item_id"});
        assertEquals("UPDATE order_items SET quantity=?, price=? WHERE order_id =? AND item_id =?", sql);
    }

    @Test
    void makeUpdateSql_withoutPk() {
        String sql = Helper.makeUpdateSql("users", new String[]{"status"}, null);
        assertEquals("UPDATE users SET status=?", sql);
    }

    @Test
    void makeUpdateSql_emptyPk() {
        String sql = Helper.makeUpdateSql("users", new String[]{"name"}, new String[0]);
        assertEquals("UPDATE users SET name=?", sql);
    }
}
