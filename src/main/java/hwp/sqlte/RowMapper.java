package hwp.sqlte;


import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Zero
 * Created on 2017/3/20.
 */
public interface RowMapper<T> extends Function<Row, T> {

    T map(Row row);

    default T apply(Row row) {
        return map(row);
    }


    class BeanMapper<T extends Object> implements RowMapper<T> {

        private Supplier<T> supplier;

        public BeanMapper(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Override
        public T map(Row row) {
            return convert(row, supplier);
        }

        static <T> T convert(Row from, Supplier<T> supplier) {
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

    class DoubleMapper implements RowMapper<Double> {
        @Override
        public Double map(Row row) {
            Object v = row.values().iterator().next();
            if (v instanceof Double) {
                return (Double) v;
            }
            Number number = (Number) v;
            return number.doubleValue();
        }
    }

    class IntMapper implements RowMapper<Integer> {

        public static final IntMapper MAPPER = new IntMapper();

        private IntMapper() {
        }

        @Override
        public Integer map(Row row) {
            Object v = row.values().iterator().next();
            if (v instanceof Integer) {
                return (Integer) v;
            }
            Number number = (Number) v;
            return number.intValue();
        }
    }

    class LongMapper implements RowMapper<Long> {

        public static final LongMapper MAPPER = new LongMapper();

        private LongMapper() {
        }

        @Override
        public Long map(Row row) {
            Object v = row.values().iterator().next();
            if (v instanceof Long) {
                return (Long) v;
            }
            Number number = (Number) v;
            return number.longValue();
        }
    }

    class NumberMapper implements RowMapper<Number> {
        @Override
        public Number map(Row row) {
            return (Number) row.values().iterator().next();
        }
    }

    class StringMapper implements RowMapper<String> {

        public static final StringMapper MAPPER = new StringMapper();

        private StringMapper() {
        }

        @Override
        public String map(Row row) {
            return Objects.toString(row.values().iterator().next());
        }
    }

}
