package hwp.sqlte;


import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Zero
 * Created on 2017/3/20.
 */
public interface RowMapper<T> extends Function<Row, T> {

    StringMapper STRING = StringMapper.MAPPER;
    IntMapper INTEGER = IntMapper.MAPPER;
    ShortMapper SHORT = ShortMapper.MAPPER;
    LongMapper LONG = LongMapper.MAPPER;
    DoubleMapper DOUBLE = DoubleMapper.MAPPER;
    FloatMapper FLOAT = FloatMapper.MAPPER;
    NumberMapper NUMBER = NumberMapper.MAPPER;
    BigDecimalMapper BIG_DECIMAL = BigDecimalMapper.MAPPER;


    T map(Row row);

    default T apply(Row row) {
        return map(row);
    }


    class FloatMapper implements RowMapper<Float> {
        static final FloatMapper MAPPER = new FloatMapper();

        @Override
        public Float map(Row row) {
            Object v = row.values().iterator().next();
            if (v instanceof Float) {
                return (Float) v;
            }
            Number number = (Number) v;
            return number.floatValue();
        }
    }

    class DoubleMapper implements RowMapper<Double> {
        static final DoubleMapper MAPPER = new DoubleMapper();

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
        static final IntMapper MAPPER = new IntMapper();

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

    class ShortMapper implements RowMapper<Short> {
        static final ShortMapper MAPPER = new ShortMapper();

        private ShortMapper() {
        }

        @Override
        public Short map(Row row) {
            Object v = row.values().iterator().next();
            if (v instanceof Short) {
                return (Short) v;
            }
            Number number = (Number) v;
            return number.shortValue();
        }
    }

    class LongMapper implements RowMapper<Long> {

        static final LongMapper MAPPER = new LongMapper();

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
        static NumberMapper MAPPER = new NumberMapper();

        @Override
        public Number map(Row row) {
            return (Number) row.values().iterator().next();
        }
    }

    class BigDecimalMapper implements RowMapper<BigDecimal> {
        static BigDecimalMapper MAPPER = new BigDecimalMapper();

        @Override
        public BigDecimal map(Row row) {
            Object v = row.values().iterator().next();
            if (v instanceof BigDecimal) {
                return (BigDecimal) v;
            }
            return new BigDecimal(v.toString());
        }
    }

    class StringMapper implements RowMapper<String> {

        static final StringMapper MAPPER = new StringMapper();

        private StringMapper() {
        }

        @Override
        public String map(Row row) {
            return Objects.toString(row.values().iterator().next());
        }
    }

}
