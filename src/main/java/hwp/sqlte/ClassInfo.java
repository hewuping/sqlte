package hwp.sqlte;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Zero
 * Created on 2018/11/13.
 */
class ClassInfo {

    private static Map<Class<?>, ClassInfo> map = new HashMap<>();

    private Map<Field, String> fieldColumnMap = new HashMap<>();
    private Map<String, Field> columnFieldMap = new HashMap<>();
    private Map<String, Field> ids = new HashMap<>(4);
    private String tableName;
    private String schema;
    private String fullTableName;
    private String[] columns;
    private String[] excludePkColumns;
    private String[] idColumns;
    private Class<?> clazz;

    static ClassInfo getClassInfo(Class<?> clazz) {
        ClassInfo info = map.get(clazz);
        if (info != null) {
            return info;
        }
        info = new ClassInfo(clazz);
        map.put(clazz, info);
        return info;
    }

    private ClassInfo(Class<?> clazz) {
        this.clazz = clazz;
        init();
    }

    private void init() {
        Field[] fields = clazz.getFields();
        for (Field field : fields) {
            if (isPublicField(field)) {
                Column column = field.getAnnotation(Column.class);
                String columnName;
                if (column == null) {
                    String fieldName = field.getName();
                    StringBuilder builder = new StringBuilder(fieldName.length());
                    for (int i = 0, len = fieldName.length(); i < len; i++) {
                        char c = fieldName.charAt(i);
                        if (Character.isUpperCase(c)) {
                            builder.append('_').append(Character.toLowerCase(c));
                        } else {
                            builder.append(c);
                        }
                    }
                    columnName = builder.toString();
                } else {
                    columnName = column.name();
                }
                this.columnFieldMap.put(columnName, field);
                this.fieldColumnMap.put(field, columnName);
                Id id = field.getAnnotation(Id.class);
                if (id != null) {
                    this.ids.put(columnName, field);
                }
            }
        }
        Table table = clazz.getAnnotation(Table.class);
        this.tableName = table == null ? clazz.getSimpleName().toLowerCase() : table.name();
        this.idColumns = this.ids.keySet().toArray(new String[0]);
        this.columns = this.columnFieldMap.keySet().toArray(new String[0]);
        int s = this.columns.length - this.ids.size();
        this.excludePkColumns = new String[s];
        if (s > 0) {
            int ss = 0;
            for (String column : this.columns) {
                if (!this.ids.containsKey(column)) {
                    this.excludePkColumns[ss++] = column;
                }
            }
        }
        if (table != null && !table.schema().isEmpty()) {
            this.schema = table.schema();
            this.fullTableName = this.schema + "." + this.tableName;
        } else {
            this.fullTableName = this.tableName;
        }
    }

    String getSinglePKColumn() {
        if (ids.size() != 1) {
            throw new UncheckedException("Non-single primary key exception, the number of PK: " + ids.size());
        }
        return ids.keySet().iterator().next();
    }

    boolean hasIds() {
        return !ids.isEmpty();
    }

    String[] getPkColumns() {
        return idColumns;
    }

    Map<String, Field> getColumnFieldMap() {
        return columnFieldMap;
    }

    Field getField(String column) {
        return columnFieldMap.get(column);
    }

    String getColumn(Field field) {
        return fieldColumnMap.get(field);
    }

    Field[] getFields() {
        return columnFieldMap.values().toArray(new Field[0]);
    }

    String[] getColumns() {
        return columns;
    }

    String[] getColumns(boolean excludePks) {
        if (excludePks) {
            return excludePkColumns;
        } else {
            return columns;
        }
    }

    String getTableName() {
        return fullTableName;
    }

    private boolean isPublicField(Field field) {
        return !Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers());
    }


}
