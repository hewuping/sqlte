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
    private String[] columns;
    private String[] excludePkColumns;
    private String[] idColumns;

    public static ClassInfo getClassInfo(Class<?> clazz) {
        ClassInfo info = map.get(clazz);
        if (info != null) {
            return info;
        }
        info = new ClassInfo();
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
                info.columnFieldMap.put(columnName, field);
                info.fieldColumnMap.put(field, columnName);
                Id id = field.getAnnotation(Id.class);
                if (id != null) {
                    info.ids.put(columnName, field);
                }
            }
        }
        Table table = clazz.getAnnotation(Table.class);
        info.tableName = table == null ? clazz.getSimpleName().toLowerCase() : table.name();
        info.idColumns = info.ids.keySet().toArray(new String[0]);
        info.columns = info.columnFieldMap.keySet().toArray(new String[0]);
        int s = info.columns.length - info.ids.size();
        info.excludePkColumns = new String[s];
        if (s > 0) {
            int ss = 0;
            for (String column : info.columns) {
                if (!info.ids.containsKey(column)) {
                    info.excludePkColumns[ss++] = column;
                }
            }
        }
        map.put(clazz, info);
        return info;
    }

    public String getSinglePKColumn() {
        if (ids.size() != 1) {
            throw new UncheckedException("Non-single primary key exception, the number of PK: " + ids.size());
        }
        return ids.keySet().iterator().next();
    }

    public boolean hasIds() {
        return !ids.isEmpty();
    }

    public String[] getPkColumns() {
        return idColumns;
    }

    public Map<String, Field> getColumnFieldMap() {
        return columnFieldMap;
    }

    public Field getField(String column) {
        return columnFieldMap.get(column);
    }

    public String getColumn(Field field) {
        return fieldColumnMap.get(field);
    }

    public Field[] getFields() {
        return columnFieldMap.values().toArray(new Field[0]);
    }

    public String[] getColumns() {
        return columns;
    }

    public String[] getColumns(boolean excludePks) {
        if (excludePks) {
            return excludePkColumns;
        } else {
            return columns;
        }
    }

    public String getTableName() {
        return tableName;
    }

    private static boolean isPublicField(Field field) {
        return !Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers());
    }


}
