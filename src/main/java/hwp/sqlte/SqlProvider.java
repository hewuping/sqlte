package hwp.sqlte;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Zero
 * Created by Zero on 2017/7/12 0012.
 */
public interface SqlProvider {

    String getSql(String key);

    static SqlProvider Default() {
        return DefaultSqlProvider.def;
    }

    class DefaultSqlProvider implements SqlProvider {
        private static Logger logger = LoggerFactory.getLogger(SqlProvider.class);
        private static DefaultSqlProvider def = new DefaultSqlProvider();

        private Map<String, String> sqlMap = new HashMap<>();

        private DefaultSqlProvider() {
        }

        @Override
        public String getSql(String sqlKey) {
            String s = sqlMap.get(sqlKey);
            if (s != null) {
                return s;
            }
            int i = sqlKey.indexOf('.');
            if (i == -1) {
                URL url = SqlProvider.class.getResource("/default.sql");
                if (url == null) {
                    throw new UncheckedIOException(new FileNotFoundException("The default.sql file is not found in classpath"));
                }
                try {
                    sqlMap.putAll(parse(url, null));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            } else {
                String prefix = sqlKey.substring(0, i);
                URL url = SqlProvider.class.getResource("/" + prefix + ".sql");
                if (url == null) {
                    throw new UncheckedIOException(new FileNotFoundException("The " + prefix + ".sql file is not found in classpath"));
                }
                try {
                    sqlMap.putAll(parse(url, prefix));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
            return sqlMap.get(sqlKey);
        }

        private Map<String, String> parse(URL url, String prefix) throws IOException {
            if (logger.isInfoEnabled()) {
                logger.info("loading sql from: {}", url);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                String line;
                Map<String, String> map = new HashMap<>();
                String key = null;
                StringBuilder sql = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) {
                        if (key != null) {
                            map.put(prefix == null ? key : prefix + "." + key, sql.toString());
                        }
                        key = null;
                        sql.setLength(0);
                    } else if (line.startsWith("-- #")) {
                        key = line.substring(4);
                    } else if (key != null && !line.startsWith("--")) {
                        if (sql.length() > 0) {
                            sql.append('\n');
                        }
                        sql.append(line);
                    }
                }
                if (key != null) {
                    map.put(prefix == null ? key : prefix + "." + key, sql.toString());
                }
                if (logger.isInfoEnabled()) {
                    map.forEach((k, v) -> {
                        logger.info("key: {},  sql: {}", k, v.replace('\n', ' '));
                    });
                }
                return map;
            }
        }
    }

}
