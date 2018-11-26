package hwp.sqlte;


import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
    LongMapper LONG = LongMapper.MAPPER;
    DoubleMapper DOUBLE = DoubleMapper.MAPPER;
    NumberMapper NUMBER = NumberMapper.MAPPER;


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
                    Field field = entry.getValue();
                    if (value != null) {
                        if (value instanceof String && !field.isEnumConstant()) {
                            Column column = field.getAnnotation(Column.class);//TODO 这一步需要优化, 因为有同步锁
                            if (column != null) {
                                Class<? extends Serializer> serializerClass = column.serializer();
                                if (serializerClass != Serializer.class) {
                                    try {
                                        //TODO 这一步需要优化, 如果是线程安全的则缓存
                                        Serializer serializer = serializerClass.getDeclaredConstructor().newInstance();
                                        value = serializer.decode((String) value);
                                    } catch (InstantiationException e) {
                                        e.printStackTrace();
                                    } catch (InvocationTargetException e) {
                                        e.printStackTrace();
                                    } catch (NoSuchMethodException e) {
                                        throw new RuntimeException("缺少无参构造器: " + serializerClass.getName());
                                    }
                                }
                            }
                        }
                        if (value.getClass() == field.getType() || field.getType().isInstance(value)) {
                            field.set(obj, value);
                        } else {
                            ConversionService conversionService = Config.getConfig().getConversionService();
                            if (conversionService.canConvert(value.getClass(), field.getType())) {
                                field.set(obj, conversionService.convert(value, field.getType()));
                            } else if (field.getType() == String.class) {
                                entry.getValue().set(obj, value.toString());
                            }
                        }
                    }
                }
                return obj;
            } catch (IllegalAccessException e) {
                throw new UncheckedException(e);
            }
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
