package hwp.sqlte;

/**
 * 对 SQL 进行修改操作
 */
public interface SqlHandler {

    SqlHandler DEFAUTL = new SqlHandler() {
        @Override
        public String handle(String sql) {
            return sql;
        }
    };

    String handle(String sql);



}
