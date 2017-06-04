package hwp.sqlte.impl;

import hwp.sqlte.Insert;
import hwp.sqlte.Query;
import hwp.sqlte.Session;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Zero
 *         Created on 2017/3/27.
 */
public class SessionImpl implements Session {
    static ThreadLocal<SessionImpl> threadLocal = new ThreadLocal<>();

    private SessionImpl() {
    }

    public static SessionImpl getSession() {
        SessionImpl session = threadLocal.get();
        if (session == null) {
            session = new SessionImpl();
            threadLocal.set(session);
        }
        session.refCount.getAndIncrement();
        return session;
    }


    private AtomicInteger refCount = new AtomicInteger();


    @Override
    public Session use(DataSource dataSource) {
        return null;
    }

    @Override
    public Session use(String ds) {
        return null;
    }

    @Override
    public Connection connection() {
        return null;
    }

    @Override
    public Query query(String sql, Object... objects) {
        return null;
    }

    @Override
    public Insert insert(String sql, Object... objects) {
        return null;
    }

    @Override
    public Insert insert(String table, Map<String, Object> map) {
        return null;
    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void close() {
        if (refCount.decrementAndGet() == 0) {
//            connection().close();
        }
    }

    @Override
    public Session useCache() {
        return this;
    }

    @Override
    public Session clearCache() {
        return null;
    }

    @Override
    public Session addCloseListener(CloseListener listener) {
        return null;
    }
}
