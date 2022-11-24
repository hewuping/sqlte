package hwp.sqlte;

import hwp.sqlte.util.NameUtils;
import hwp.sqlte.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * @author Zero
 * Created on 2018/11/13.
 */
public class ClassInfo {
    private static final Map<Class<?>, ClassInfo> map = new HashMap<>();

    private final Class<?> clazz;
    private String schema;
    private String tableName;
    private String fullTableName;
    private Field[] fields;
    private String[] columns;
    private String[] pkColumns;
    private String[] autoGenerateColumns;
    private String[] insertColumns;//排除自动生成的列
    private String[] updateColumns;

    private final Map<Field, String> fieldColumnMap = new HashMap<>();
    private final Map<String, String> fieldNameColumnMap = new HashMap<>();
    private final Map<String, Field> columnFieldMap = new LinkedHashMap<>();

//    private Map<String, Class<?>> typeMap = new HashMap<>();

    public static ClassInfo getClassInfo(Class<?> clazz) {
        Objects.requireNonNull(clazz);
        ClassInfo info = map.get(clazz);
        if (info != null) {
            return info;
        }
        synchronized (map) {
            info = new ClassInfo(clazz);
            map.put(clazz, info);
        }
        return info;
    }

    private ClassInfo(Class<?> clazz) {
        this.clazz = clazz;
        init();
    }

    private void init() {
        //fields
        List<String> pkColumnList = new ArrayList<>(4);
        List<String> autoGenerateColumnList = new ArrayList<>(2);
        List<String> updateColumnList = new ArrayList<>();
        List<String> insertColumnList = new ArrayList<>();
        Field[] fields = clazz.getFields();
        for (Field field : fields) {
            if (isIgnore(field)) {
                continue;
            }
            Column column = field.getAnnotation(Column.class);
            String columnName;
            if (column == null || column.name().isEmpty()) {
                columnName = NameUtils.toUnderscore(field.getName());
            } else {
                columnName = column.name();
            }
            this.columnFieldMap.put(columnName, field);
            this.fieldColumnMap.put(field, columnName);
            this.fieldNameColumnMap.put(field.getName(), columnName);
            Id id = field.getAnnotation(Id.class);
            if (id != null) {
                pkColumnList.add(columnName);
                if (id.generate()) {
                    autoGenerateColumnList.add(columnName);
                }
            }
            // ID 暂时设置为不可更新
            if (id == null && (column == null || column.update())) {
                updateColumnList.add(columnName);
            }
            if (id == null || !id.generate()) {
                insertColumnList.add(columnName);
            }
        }

        //columns, fields
        Counter index = new Counter();
        this.columns = new String[columnFieldMap.size()];
        this.fields = new Field[columnFieldMap.size()];
        columnFieldMap.forEach((column, field) -> {
            this.columns[index.get()] = column;
            this.fields[index.get()] = field;
            index.add(1);
        });
        this.pkColumns = pkColumnList.toArray(new String[0]);
        this.autoGenerateColumns = autoGenerateColumnList.toArray(new String[0]);
        //insert Columns
        this.insertColumns = insertColumnList.toArray(new String[0]);
        //Updateable Columns
        this.updateColumns = updateColumnList.toArray(new String[0]);
        //Table Name
        Table table = clazz.getAnnotation(Table.class);
        this.tableName = table != null ? table.name() : StringUtils.toUnderscore(clazz.getSimpleName());
        if (table != null && !table.schema().isEmpty()) {
            this.schema = table.schema();
            this.fullTableName = this.schema + "." + this.tableName;
        } else {
            this.fullTableName = this.tableName;
        }
    }

    public String className() {
        return clazz.getName();
    }

    public String getPKColumn() {// getPrimaryKeyColumn
        if (pkColumns.length == 0) {
            throw new SqlteException("Undefined ID field: " + clazz.getName());
        }
        return pkColumns[0];
    }

    boolean hasIds() {
        return pkColumns.length > 0;
    }

    String[] getPkColumns() {
        return pkColumns;
    }

    Map<String, Field> getColumnFieldMap() {
        return columnFieldMap;
    }

    /**
     * 通过列名获取 Field
     *
     * @param column
     * @return
     */
    public Field getField(String column) {
        return columnFieldMap.get(column);
    }

    /**
     * 通过列名获取多个属性值
     *
     * @param obj
     * @param columns
     * @return
     */
    Object[] getValuesByColumn(Object obj, String[] columns) {
        Objects.requireNonNull(obj, "'obj' is null");
        Objects.requireNonNull(columns, "'columns' is null");
        Object[] values = new Object[columns.length];
        for (int i = 0; i < columns.length; i++) {
            String column = columns[i];
            Field field = this.getField(column);
            Object value = Helper.getSerializedValue(obj, field);
            values[i] = value;
        }
        return values;
    }

    Object getValueByColumn(Object obj, String column) {
        Objects.requireNonNull(obj, "'obj' is null");
        Objects.requireNonNull(column, "'column' is null");
        Field field = this.getField(column);
        return Helper.getSerializedValue(obj, field);
    }

    public String getColumn(Field field) {
        return fieldColumnMap.get(field);
    }

    public String getColumn(String fieldName) {
        return fieldNameColumnMap.get(fieldName);
    }

    public Field[] getFields() {
        return fields;
    }

    public String[] getColumns() {
        return columns;
    }

    public String[] getInsertColumns() {
        return insertColumns;
    }

    public String[] getUpdateColumns() {
        return updateColumns;
    }

    public String getTableName() {
        return fullTableName;
    }

    public String[] getAutoGenerateColumns() {
        return autoGenerateColumns;
    }

    private boolean isIgnore(Field field) {
        return Modifier.isStatic(field.getModifiers())
                || Modifier.isFinal(field.getModifiers())
                || Modifier.isNative(field.getModifiers())
                || Modifier.isTransient(field.getModifiers())
                || Modifier.isPrivate(field.getModifiers())
                || Modifier.isProtected(field.getModifiers())
                || field.getAnnotation(Ignore.class) != null;
    }


}
