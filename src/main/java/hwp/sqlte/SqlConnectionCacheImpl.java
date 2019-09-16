/*
 * Copyright (c) 2019. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 *       Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 *       Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 *       Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 *       Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package hwp.sqlte;

import hwp.sqlte.cache.Cache;

import java.sql.Connection;

/**
 * @author Zero
 * Created on 2019/9/12.
 */
class SqlConnectionCacheImpl extends SqlConnectionImpl {
    private Cache cache;

    SqlConnectionCacheImpl(Connection conn, Cache cache) {
        super(conn);
        this.cache = cache;
    }

    @Override
    public SqlConnection cacheable() {
        return this;
    }

    @Override
    public SqlResultSet query(String sql) throws UncheckedSQLException {
        return this.query(new SimpleSql(sql));
    }

    @Override
    public SqlResultSet query(String sql, Object... args) throws UncheckedSQLException {
        return this.query(new SimpleSql(sql, args));
    }

    @Override
    public SqlResultSet query(Sql sql) throws UncheckedSQLException {
        SqlResultSet rs = (SqlResultSet) cache.get(sql.id());
        if (rs == null) {
            rs = super.query(sql);
            cache.put(sql.id(), rs);
        }
        return rs;
    }

}
