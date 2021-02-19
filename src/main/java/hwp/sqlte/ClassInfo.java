package hwp.sqlte;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * @author Zero
 * Created on 2018/11/13.
 */
class ClassInfo {
    private static final Map<Class<?>, ClassInfo> map = new HashMap<>();

    private final Class<?> clazz;
    private String tableName;
    private String schema;
    private String fullTableName;
    private String[] columns;
    private Field[] fields;
    private String[] pkColumns;
    private String[] nonPkColumns;
    private String[] autoGenerateColumns;
    private String[] nonAutoGenerateColumns;
    private Field[] nonAutoGenerateFields;

    private final Map<Field, String> fieldColumnMap = new HashMap<>();
    private final Map<String, Field> columnFieldMap = new HashMap<>();
    private final Map<String, Field> pks = new HashMap<>(4);

//    private Map<String, Class<?>> typeMap = new HashMap<>();

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
        Map<String, Field> columnFieldMap0 = new LinkedHashMap<>();
        for (Field field : fields) {
            if (isPersistent(field)) {
                Column column = field.getAnnotation(Column.class);
                String columnName;
                if (column == null || column.name().isEmpty()) {
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
                columnFieldMap0.put(columnName, field);
                this.fieldColumnMap.put(field, columnName);
                Id id = field.getAnnotation(Id.class);
                if (id != null) {
                    this.pks.put(columnName, field);
                    if (id.generate()) {
                        _autoGenerateColumns.add(columnName);
                    }
                }
            }
        }

        Table table = clazz.getAnnotation(Table.class);
        this.tableName = table == null ? clazz.getSimpleName().toLowerCase() : table.name();
        this.pkColumns = this.pks.keySet().toArray(new String[0]);
        this.autoGenerateColumns = _autoGenerateColumns.toArray(new String[0]);


        //columns, fields
        UnsafeCount index = new UnsafeCount();
        this.columns = new String[columnFieldMap0.size()];
        this.fields = new Field[columnFieldMap0.size()];
        columnFieldMap0.forEach((k, v) -> {
            this.columns[index.get()] = k;
            this.fields[index.get()] = v;
            index.add(1);
        });
        this.columnFieldMap.putAll(columnFieldMap0);
        columnFieldMap0.clear();
        //nonPkColumns
        this.nonPkColumns = Arrays.stream(this.columns).filter(s -> !pks.containsKey(s)).toArray(String[]::new);
        //nonAutoGenerateColumns
        this.nonAutoGenerateColumns = Arrays.stream(this.columns).filter(s -> !_autoGenerateColumns.contains(s)).toArray(String[]::new);
        //nonAutoGenerateFields
        this.nonAutoGenerateFields = new Field[this.nonAutoGenerateColumns.length];
        for (int i = 0; i < nonAutoGenerateColumns.length; i++) {
            String column = nonAutoGenerateColumns[i];
            this.nonAutoGenerateFields[i] = getField(column);
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
        if (pks.size() != 1) {
            throw new UncheckedException("Undefined ID field: " + clazz.getName());
        }
        return pks.keySet().iterator().next();
    }

    boolean hasIds() {
        return !pks.isEmpty();
    }

    String[] getPkColumns() {
        return pkColumns;
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


    public String[] getNonPkColumns() {
        return nonPkColumns;
    }

    public String[] getNonAutoGenerateColumns() {
        return nonAutoGenerateColumns;
    }

    public Field[] getNonAutoGenerateFields() {
        return nonAutoGenerateFields;
    }

    String getTableName() {
        return fullTableName;
    }

    public String[] getAutoGenerateColumns() {
        return autoGenerateColumns;
    }

    private boolean isPersistent(Field field) {
        return !Modifier.isStatic(field.getModifiers())
                && !Modifier.isFinal(field.getModifiers())
                && !Modifier.isNative(field.getModifiers())
                && !Modifier.isTransient(field.getModifiers());
    }


}
