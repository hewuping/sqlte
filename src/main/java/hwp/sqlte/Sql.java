package hwp.sqlte;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Zero
 * Created on 2017/3/22.
 */
public interface Sql {

    /**
     * Get standard sql
     *
     * @return sql
     */
    String sql();

    /**
     * Get args
     *
     * @return
     */
    Object[] args();

    default String id() {
        if (args() == null || args().length == 0) {
            return sql();
        }
        return sql().concat("@").concat(Arrays.toString(args()));
    }


    static Sql create(String sql, Object... args) {
        return new SimpleSql(sql, args);
    }

    static Config config() {
        return Config.getConfig();
    }

    static void config(Consumer<Config> consumer) {
        consumer.accept(Config.getConfig());
    }

    static SqlConnection open() throws SqlteException {
        try {
            return SqlConnectionImpl.use(config().getDataSource().getConnection());
        } catch (SQLException e) {
            throw new SqlteException(e);
        }
    }

    static SqlConnection open(String dsName) throws SqlteException {
        try {
            return SqlConnectionImpl.use(config().getDataSource(dsName).getConnection());
        } catch (SQLException e) {
            throw new SqlteException(e);
        }
    }

    static SqlConnection open(DataSource dataSource) throws SqlteException {
        try {
            return SqlConnectionImpl.use(dataSource.getConnection());
        } catch (SQLException e) {
            throw new SqlteException(e);
        }
    }

    static void use(DataSource dataSource, Consumer<SqlConnection> consumer) throws SqlteException {
        try (SqlConnection conn = SqlConnectionImpl.use(dataSource.getConnection())) {
            consumer.accept(conn);
        } catch (SQLException e) {
            throw new SqlteException(e);
        }
    }

    static void use(Consumer<SqlConnection> consumer) throws SqlteException {
        try (SqlConnection conn = open()) {
            consumer.accept(conn);
        }
    }

    static <R> R apply(Function<SqlConnection, R> function) throws SqlteException {
        try (SqlConnection conn = open()) {
            return function.apply(conn);
        }
    }

    static <R> R transaction(Function<SqlConnection, R> function) throws SqlteException {
        SqlConnection conn = open();
        try {
            conn.setAutoCommit(false);
            R r = function.apply(conn);
            conn.commit();
            return r;
        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.close();
        }
    }

    static void transaction(Consumer<SqlConnection> consumer) throws SqlteException {
        SqlConnection conn = open();
        try {
            conn.setAutoCommit(false);
            consumer.accept(conn);
            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            throw new SqlteException(e);
        } finally {
            conn.close();
        }
    }


}
