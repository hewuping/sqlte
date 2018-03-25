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

    private Sql sql;
    private Connection connection;
    private SqlResultSet resultSet;
    private boolean executed;
    private SQLException err;

    protected Query(Sql sql) {
        this.sql = sql;
    }

    protected Query execute() throws SQLException {
        if (executed) {
            if (err == null) {
                return this;
            }
            throw err;
        }
        executed = true;
        try {
            PreparedStatement statement = connection.prepareStatement(sql.sql());
            Helper.fillStatement(statement, sql.args());
            this.resultSet = Helper.convert(statement.executeQuery());
            return this;
        } catch (SQLException e) {
            err = e;
            throw e;
        }
    }

    public <T> List<T> list(RowMapper<T> mapper) throws SQLException {
        return execute().resultSet.getRows().stream().map(mapper::map).collect(Collectors.toList());
    }

    public List<String> listAsString() throws SQLException {
        return list(StringMapper.MAPPER);
    }

    public <T> Optional<T> first(RowMapper<? extends T> mapper) throws SQLException {
        execute();
        if (resultSet.getRows().isEmpty()) {
            return Optional.empty();
        }
        T map = mapper.map(resultSet.getRows().get(0));
        return Optional.ofNullable(map);
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
        } else if (survivalTime > 0) {

        }
        return this;
    }


}
