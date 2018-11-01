package hwp.sqlte;

import java.lang.reflect.Field;

/**
 * @author Zero
 *         Created by Zero on 2017/8/13 0013.
 */
public interface NameConverter {
    NameConverter DEFAULT = new DefaultNameConverter();

    String columnName(Field field);

    String fieldName(String column);

    String tableName(String className);

    String quoteTableName(String table);

    String quoteColumnName(String column);


    class DefaultNameConverter implements NameConverter{

        @Override
        public String columnName(Field field) {
            return field.getName();
        }

        @Override
        public String fieldName(String column) {
            return column;
        }

        @Override
        public String tableName(String className) {
            return className.toLowerCase();
        }

        @Override
        public String quoteTableName(String table) {
            return table;
        }

        @Override
        public String quoteColumnName(String column) {
            return column;
        }
    }

}
