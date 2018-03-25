package hwp.sqlte.util;

import hwp.sqlte.Row;
import hwp.sqlte.RowMapper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Zero
 *         Created by Zero on 2017/8/17 0017.
 */
public class JSBC {

    public static <T> void firstMap(ResultSet resultSet, Supplier<T> supplier) throws SQLException, IllegalAccessException {
        T t = supplier.get();
        Field[] fields = t.getClass().getFields();//只映射public字段，public字段必须有
        if (resultSet.next()) {
            for (Field field : fields) {
                boolean b = Modifier.isPublic(field.getModifiers())
                        && Modifier.isFinal(field.getModifiers());
                //NameConverter
                Object value = resultSet.getObject(field.getName(), field.getType());
                field.set(t,value);
            }
        }
    }

}
