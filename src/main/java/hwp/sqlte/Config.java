package hwp.sqlte;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Zero
 *         Created on 2018/4/13.
 */
final public class Config {
    protected static Config config = new Config();
    protected static Properties SQLS = new Properties();

    protected static Map<String, DataSource> dataSourceMap = new HashMap<>();

    private Config() {

    }

    private DataSource def;

    public Config setDataSource(DataSource dataSource) {
        this.def = dataSource;
        return setDataSource("default", dataSource);
    }

    public Config setDataSource(String name, DataSource dataSource) {
        dataSourceMap.put(name, dataSource);
        return this;
    }

    public DataSource getDataSource() {
        return def;
    }

    public DataSource getDataSource(String name) {
        return dataSourceMap.get(name);
    }


}
