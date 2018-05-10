package hwp.sqlte;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.function.Function;

/**
 * @author Zero
 *         Created on 2017/3/22.
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
        return sql().concat("@").concat(Arrays.toString(args()));
    }


    static Sql create(String sql, Object... args) {
        return new SimpleSql(sql, args);
    }

    static Config config() {
        return Config.config;
    }

    static SqlConnection newConnection() {
        try {
            return SqlConnection.use(config().getDataSource().getConnection());
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }

    static SqlConnection newConnection(String dsName) {
        try {
            return SqlConnection.use(config().getDataSource(dsName).getConnection());
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }

    /*  static <T> T runOnTx(SqlFunction<Tx, T> function) throws Exception {
          Tx tx = Tx.createOrGet();
          try {
              tx.begin();
              return function.apply(tx);
          } finally {
              tx.end();
          }
      }
  */
    static <T> T useTx(Function<SqlConnection, T> function) throws Exception {
        SqlConnection connection = newConnection();
        try {
            return function.apply(connection);
        } catch (Exception e) {
            connection.rollback();
            throw e;
        } finally {
            connection.close();
        }
    }


}
