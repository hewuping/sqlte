package hwp.sqlte;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * @author Zero
 *         Created on 2017/3/21.
 */
public interface Session {

    Session use(DataSource dataSource);

    Session use(String ds);

    Connection connection();

    Query query(String sql, Object... objects);

    Insert insert(String sql, Object... objects);

    void onError(Throwable throwable);

    void close();

    Session addCloseListener(CloseListener listener);

    interface CloseListener {
        void onClose(Session session);
    }
}
