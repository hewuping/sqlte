package hwp.sqlte;

public class SqlTx {

    private static final ThreadLocal<SqlConnection> threadLocal = new ThreadLocal<>();
    private static final Object[] lock = new Object[0];


    public static SqlConnection getCurrent() {
        return getCurrent(true);
    }

    public static SqlConnection getCurrent(boolean create) {
        SqlConnection conn = threadLocal.get();
        if ((conn == null || conn.isClosed()) && create) {
            synchronized (lock) {
                conn = Sql.open();
                threadLocal.set(conn);
            }
        }
        return conn;
    }

    public static SqlConnection begin() {
        SqlConnection conn = getCurrent(true);
        conn.beginTransaction();
        return conn;
    }

    public static void commit() {
        SqlConnection current = getCurrent(false);
        if (current != null) {
            current.commit();
        }
    }

}
