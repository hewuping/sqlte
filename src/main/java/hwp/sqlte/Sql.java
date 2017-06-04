package hwp.sqlte;

import hwp.sqlte.mapper.StringMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

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


    ThreadLocal<SqlConnection> THREAD_LOCAL = new ThreadLocal<>();
    Resource<DataSource> DATA_SOURCE_RESOURCE = new Resource<>();

    static <T> T runOnTx(SqlFunction<SqlConnection, T> function) throws Exception {
        try (SqlConnection connection = connection(null)) {
            connection.setAutoCommit(true);
            try {
                T rs = function.apply(connection);
                connection.commit();
                return rs;
            } catch (Exception e) {
                log.error("rollback error", e);
                try {
                    connection.rollback();
                } catch (SQLException e1) {
                    //ignore
                    log.error("rollback error", e1);
                    //TODO logger.error(e); logger.error(e1);
                    throw e1;
                }
                throw e;
            } finally {
                try {
                    connection.close();//在已知的事务边界的情况下必须要关闭连接
                } catch (SQLException e1) {
                    //ignore
                }
            }
        }
    }

    static <T> T exec(SqlFunction<SqlConnection, T> function) throws Exception {
        return exec(null, function);
    }

    static <T> T exec(SqlConnection connection, SqlFunction<SqlConnection, T> function) throws Exception {
        connection = connection(connection);
        if (connection.getAutoCommit()) {
            try {
                return function.apply(connection);
            } finally {
                connection.close();
            }
        } else {
            try {
                return function.apply(connection);
            } catch (SQLException e) {
                try {
                    connection.rollback();
                } catch (SQLException e1) {
                    //ignore
                    //TODO 回滚失败
                }
                try {
                    connection.close();//如果发生了异常则关闭连接
                } catch (SQLException e1) {
                    //ignore
                }
                throw e;
            }
        }
    }

    static SqlConnection connection(SqlConnection connection) throws SQLException {
        if (connection == null) {
            connection = THREAD_LOCAL.get();
            if (connection == null) {
                Objects.requireNonNull(DATA_SOURCE_RESOURCE.get(), "Datasource is not defined");
                connection = SqlConnection.warp(DATA_SOURCE_RESOURCE.get().getConnection());
                THREAD_LOCAL.set(connection);
            }
        }
        return connection;
    }

    static PreparedStatement prep(SqlConnection conn, String sql, Object... args) throws SQLException {
        try (PreparedStatement stat = conn.prepareStatement(sql)) {
            Helper.fillStatement(stat, args);
            return stat;
        }
    }


    public static void main(String[] args) throws Exception {
        Object[] obj = new Object[]{1, "A"};
        System.out.println(obj);
        exec(conn -> {
//            prep(conn,"",)
            ResultSet rs = conn.query("select * from user");
            ResultSet rs3 = conn.query("#select * from user");
            List list;
            rs.stream().map(StringMapper.MAPPER);

            System.out.println(conn);
            return "";
        });
    }

}
