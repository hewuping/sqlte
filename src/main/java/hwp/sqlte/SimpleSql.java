package hwp.sqlte;

/**
 * @author Zero
 * Created on 2018/4/30.
 */
public class SimpleSql implements Sql {

    private String sql;
    private Object[] args;

    public SimpleSql(String sql, Object... args) {
        this.sql = sql;
        this.args = args;
    }

    @Override
    public String sql() {
        return sql;
    }

    @Override
    public Object[] args() {
        return args;
    }

}
