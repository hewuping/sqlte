package hwp.sqlte;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    Logger log = LoggerFactory.getLogger(Sql.class);

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

    static SqlConnection open() throws UncheckedSQLException {
        try {
            return SqlConnectionImpl.use(config().getDataSource().getConnection());
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }

    static SqlConnection open(String dsName) throws UncheckedSQLException {
        try {
            return SqlConnectionImpl.use(config().getDataSource(dsName).getConnection());
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }

    static SqlConnection open(DataSource dataSource) throws UncheckedSQLException {
        try {
            return SqlConnectionImpl.use(dataSource.getConnection());
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }

    static void use(DataSource dataSource, Consumer<SqlConnection> consumer) throws UncheckedSQLException {
        try (SqlConnection conn = SqlConnectionImpl.use(dataSource.getConnection())) {
            consumer.accept(conn);
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }

    static void use(Consumer<SqlConnection> consumer) throws UncheckedSQLException {
        try (SqlConnection conn = open()) {
            consumer.accept(conn);
        }
    }

    static <R> R apply(Function<SqlConnection, R> function) throws UncheckedSQLException {
        try (SqlConnection conn = open()) {
            return function.apply(conn);
        }
    }

    static <R> R transaction(Function<SqlConnection, R> function) throws UncheckedSQLException {
        SqlConnection connection = open();
        try {
            connection.setAutoCommit(false);
            R r = function.apply(connection);
            connection.commit();
            return r;
        } catch (Exception e) {
            connection.rollback();
            throw e;
        } finally {
            connection.close();
        }
    }


}
