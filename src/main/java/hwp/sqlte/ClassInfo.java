package hwp.sqlte;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private Field[] fields;
    private String[] excludePkColumns;
    private String[] idColumns;
    private Class<?> clazz;
    private String[] autoGenerateColumns;

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
        //fields
        List<String> _autoGenerateColumns = new ArrayList<>(2);
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
                    if (id.generate()) {
                        _autoGenerateColumns.add(columnName);
                    }
                }
            }
        }

        Table table = clazz.getAnnotation(Table.class);
        this.tableName = table == null ? clazz.getSimpleName().toLowerCase() : table.name();
        this.idColumns = this.ids.keySet().toArray(new String[0]);
        this.autoGenerateColumns = _autoGenerateColumns.toArray(new String[0]);

        //columns, fields
        UnsafeCount index = new UnsafeCount();
        this.columns = new String[this.columnFieldMap.size()];
        this.fields = new Field[this.columnFieldMap.size()];
        this.columnFieldMap.forEach((k, v) -> {
            this.columns[index.get()] = k;
            this.fields[index.get()] = v;
            index.add(1);
        });

        //excludePkColumns
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

        //fullTableName
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
        return fields;
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

    public String[] getAutoGenerateColumns() {
        return autoGenerateColumns;
    }

    private boolean isPublicField(Field field) {
        return !Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers());
    }


}
