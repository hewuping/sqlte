package hwp.sqlte;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Zero
 *         Created on 2017/3/20.
 */
public class Row extends HashMap<String, Object> {

    public String getString(String name) {
        return (String) get(name);
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

    public <T> T map(RowMapper<T> mapper) throws SQLException {
        return mapper.map(this);
    }

    public Row set(String name, Object val) {
        put(name, val);
        return this;
    }

//    public static <T> T map(Row row, T t) {//copy
//        return row.map(t);
//    }

    public <T> T convert(T t) {//copy
        //只映射public字段，public字段必须有
        Field[] fields = t.getClass().getFields();
        for (Field field : fields) {
            //FieldNameConverter
            Object value = get(field.getName());
            try {
                field.set(t, value);
            } catch (Exception e) {
                //ignore
            }
        }
        return t;
    }

    public static void main(String[] args) {
        Row row = new Row();
        int v = row.getValue("ss", -1);
    }


}
