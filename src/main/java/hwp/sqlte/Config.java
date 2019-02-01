package hwp.sqlte;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Zero
 * Created on 2018/4/13.
 */
final public class Config {
    protected static Config config = new Config();
    private SqlProvider sqlProvider = SqlProvider.Default();
    private Options options = new Options();


    private Map<String, DataSource> dataSourceMap = new HashMap<>();

    private Config() {

    }

    public static Config getConfig() {
        return config;
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

    public SqlProvider getSqlProvider() {
        return sqlProvider;
    }

    public ConversionService getConversionService() {
        return ConversionService.DEFAULT;
    }

    public Config setSqlProvider(SqlProvider sqlProvider) {
        Objects.requireNonNull(sqlProvider, "sqlProvider can not be null");
        this.sqlProvider = sqlProvider;
        return this;
    }

    public Options options() {
        return options;
    }

    public class Options {
        public boolean batchInsertIgnoreError = false;
    }

}
