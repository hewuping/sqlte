package hwp.sqlte;


import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author Zero
 * Created on 2017/3/20.
 */
public class Row extends HashMap<String, Object> {

    public String getString(String name) {
        return (String) get(name);
    }

    public Number getNumber(String name) {
        return (Number) get(name);
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(String name, T defValue) {
        T v = (T) super.get(name);
        return v == null ? defValue : v;
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(String name) {
        return (T) super.get(name);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> getOptValue(String name) {
        return Optional.ofNullable((T) super.get(name));
    }

    public <T> T map(RowMapper<T> mapper) {
        return mapper.map(this);
    }


    public <T> T map(Supplier<T> supplier) {
        return RowMapper.BeanMapper.convert(this, supplier);
    }

    public Row set(String name, Object val) {
        put(name, val);
        return this;
    }

    public static Row from(ResultSet rs) {
        try {
            Row row = new Row();
            ResultSetMetaData metaData = rs.getMetaData();
            int cols = metaData.getColumnCount();
            for (int i = 1; i <= cols; i++) {
                row.put(metaData.getColumnLabel(i).intern(), rs.getObject(i));
            }
            return row;
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }

    public <T> T copyTo(T bean) {
        try {
            ClassInfo info = ClassInfo.getClassInfo(bean.getClass());
            for (Map.Entry<String, Field> entry : info.getColumnFieldMap().entrySet()) {
                Object value = getValue(entry.getKey());
                if (value != null) {
                    entry.getValue().set(bean, value);
                }
            }
        } catch (IllegalAccessException e) {
            return bean;
        }
        return bean;
    }

}
