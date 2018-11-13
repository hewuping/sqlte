package hwp.sqlte.mapper;


import hwp.sqlte.ClassInfo;
import hwp.sqlte.Row;
import hwp.sqlte.RowMapper;
import hwp.sqlte.UncheckedException;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author Zero
 * Created on 2017/3/21.
 */
public class BeanMapper<T extends Object> implements RowMapper<T> {

    private Supplier<T> supplier;

    public BeanMapper(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public T map(Row row) {
        return convert(row, supplier);
    }

    public static <T> T convert(Row from, Supplier<T> supplier) {
        try {
            T obj = supplier.get();
            ClassInfo info = ClassInfo.getClassInfo(obj.getClass());
            for (Map.Entry<String, Field> entry : info.getColumnFieldMap().entrySet()) {
                Object value = from.getValue(entry.getKey());
                if (value != null) {
                    entry.getValue().set(obj, value);
                }
            }
            return obj;
        } catch (IllegalAccessException e) {
            throw new UncheckedException(e);
        }
    }

}
