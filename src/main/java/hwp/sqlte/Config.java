package hwp.sqlte;

import hwp.sqlte.cache.Cache;
import hwp.sqlte.cache.LruCache;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

/**
 * @author Zero
 * Created on 2018/4/13.
 */
final public class Config {
    private static Config config = new Config();
    private final Cache DEFAULT_CACHE = new LruCache(1024);
    private SqlProvider sqlProvider = SqlProvider.Default();
    private JsonSerializer jsonSerializer = new GsonSerializer();
    private TimeZone databaseTimeZone = TimeZone.getDefault();//TODO from DB

    private Cache cache;


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

    public TimeZone getDatabaseTimeZone() {
        return databaseTimeZone;
    }

    public void setDatabaseTimeZone(TimeZone databaseTimeZone) {
        this.databaseTimeZone = databaseTimeZone;
    }

    public SqlProvider getSqlProvider() {
        return sqlProvider;
    }

    public ConversionService getConversionService() {
        return DefaultConversionService.INSTANCE;
    }

    public Config setSqlProvider(SqlProvider sqlProvider) {
        Objects.requireNonNull(sqlProvider, "sqlProvider can not be null");
        this.sqlProvider = sqlProvider;
        return this;
    }

    public Cache getCache() {
        return cache == null ? DEFAULT_CACHE : cache;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    public void setJsonSerializer(JsonSerializer jsonSerializer) {
        this.jsonSerializer = jsonSerializer;
    }

    public JsonSerializer getJsonSerializer() {
        if (jsonSerializer == null) {
            throw new ConfigException("请配置jsonSerializer");
        }
        return jsonSerializer;
    }
}
