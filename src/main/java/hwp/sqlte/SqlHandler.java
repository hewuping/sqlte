package hwp.sqlte;

import java.util.function.Function;

/**
 * 对 SQL 进行修改操作
 */
public interface SqlHandler {

    String handle(String sql);

}
