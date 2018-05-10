package hwp.sqlte;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Zero
 *         Created on 2018/4/13.
 */
@Deprecated
public class Tx {

    private static ThreadLocal<Tx> txThreadLocal = new ThreadLocal<>();
    private int refCount;
    private Map<String, SqlConnection> connMap = new HashMap<>(2);
    private Consumer<Throwable> exceptionHandler;

    public static Tx createOrGet() {
        Tx tx = txThreadLocal.get();
        if (tx == null) {
            tx = new Tx();
            txThreadLocal.set(tx);
        }
        return tx;
    }

    public void begin() {
        refCount++;
    }

    public SqlConnection connection(String dsName) {
        SqlConnection conn = connMap.get(dsName);
        if (conn == null) {
            conn = Sql.newConnection(dsName);
            try {
                conn.setAutoCommit(false);
            } catch (SQLException e) {
                throw new UncheckedSQLException(e);
            }
            connMap.put(dsName, conn);
        }
        return conn;
    }

    public void end() {
        refCount--;
        if (refCount == 0) {
            txThreadLocal.remove();
            connMap.values().forEach(conn -> {
                try {
                    conn.close();
                } catch (Exception e) {
                    if (exceptionHandler != null) {
                        exceptionHandler.accept(e);
                    } else {
                        //logger
                    }
                }
            });
        }
    }

    public void onError(Consumer<Throwable> consumer) {
        exceptionHandler = consumer;
    }

}
