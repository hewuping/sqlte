package hwp.sqlte;

import hwp.sqlte.impl.SessionImpl;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Zero
 *         Created on 2017/3/21.
 */
public interface Session {

    static <T> T runOnTx(LionFunction<Session, T> function) {
        Session session = SessionImpl.getSession();
        try {
            return function.apply(session);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        } finally {
            session.close();
        }
    }

    static Session getSession() {
        Session session = SessionImpl.getSession();
        return session;
    }

    Session use(DataSource dataSource);

    Session use(String ds);

    Session useCache();

    Session clearCache();

    Connection connection();

    Query query(String sql, Object... objects);

    Insert insert(String sql, Object... objects);

    Insert insert(String table, Map<String, Object> map);

    void onError(Throwable throwable);

    void close();

    Session addCloseListener(CloseListener listener);

    interface CloseListener {
        void onClose(Session session);
    }
}
