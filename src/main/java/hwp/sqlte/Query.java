package hwp.sqlte;


import hwp.sqlte.mapper.StringMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Zero
 *         Created on 2017/3/20.
 */
public class Query {

    private String sql;
    private Connection connection;
    private Session session;
    private ResultSet resultSet;
    private Object[] args;

    protected Query(Session session, String sql, Object... args) {
        this.session = session;
        this.sql = sql;
        this.args = args;
        this.connection = session.connection();//当前连接
    }

    protected Query execute() throws SQLException {
        if (resultSet == null) {
            PreparedStatement statement = connection.prepareStatement(sql);
            Helper.fillStatement(statement, this.args);
            this.resultSet = Helper.convert(statement.executeQuery(sql));
        }
        return this;
    }

    public <T> List<T> list(RowMapper<T> mapper) throws SQLException {
        execute();
        return execute().resultSet.getRows().stream().map(mapper::map).collect(Collectors.toList());
    }

    public List<String> listAsString() throws SQLException {
        execute();
        return resultSet.getRows().stream().map(StringMapper.MAPPER::map).collect(Collectors.toList());
    }

    public <T> Optional<T> first(RowMapper<T> mapper) throws SQLException {
        execute();
        if (resultSet.getRows().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable((T) resultSet.getRows().get(0));
    }

    public Query cacheIf(boolean b, long survivalTime) throws SQLException {
        if (b) {
            cache(survivalTime);
        }
        return this;
    }

    public Query cache(long survivalTime) throws SQLException {
        execute();
        this.resultSet.unmodifiableRows();
        //本地缓存session之后清理,
        if (survivalTime == 0) {
            session.addCloseListener(session1 -> {
                //TODO
            });
        } else if (survivalTime > 0) {

        }
        return this;
    }




    public static void main(String[] args) throws SQLException {
        Query query = new Query(null, "");
        query.cache(0).list(row -> {
            return "";
        });
        Optional<String> first = query.first(resultSet -> "Hello");
        query.first(result -> {

            return "";
        });
    }

}
