package hwp.sqlte;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * @author Zero
 * Created on 2017/4/11.
 */
public class DataSourceWrapper implements DataSource {

    private DataSource delegage;
    private ThreadLocal<Connection> threadLocal = new ThreadLocal<>();

    public DataSourceWrapper(DataSource delegage) {
        this.delegage = delegage;
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = threadLocal.get();
        if (connection == null || connection.isClosed()) {
            connection = delegage.getConnection();
        }
        return connection;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Connection connection = threadLocal.get();
        if (connection == null || connection.isClosed()) {
            connection = delegage.getConnection(username, password);
        }
        return connection;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return delegage.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        delegage.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        delegage.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return delegage.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return delegage.getParentLogger();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (isWrapperFor(iface)) {
            return iface.cast(this);
        } else {
            throw new SQLException("unwrap failed for:" + iface);
        }
    }

    @Override
    public boolean isWrapperFor(Class<?> interfaces) throws SQLException {
        return interfaces.isInstance(this);
    }

}
