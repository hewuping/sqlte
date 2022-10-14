package hwp.sqlte;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.Supplier;

class BeanMapper<T> implements RowMapper<T> {
    private static final Logger logger = LoggerFactory.getLogger(BeanMapper.class);

    private final Supplier<T> supplier;

    public BeanMapper(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public BeanMapper(Class<T> clazz) {
        this.supplier = Helper.toSupplier(clazz);
    }

    static <T> T copy(Row row, T obj) throws SqlteException {
        try {
            ClassInfo info = ClassInfo.getClassInfo(obj.getClass());
            for (Map.Entry<String, Field> entry : info.getColumnFieldMap().entrySet()) {
                Object dbValue = row.getValue(entry.getKey());
                Field field = entry.getValue();
                if (dbValue != null) {
                    // JSON对象转换
                    if (dbValue instanceof String && !field.isEnumConstant()) {
                        Column column = field.getAnnotation(Column.class);
                        String _dbValue = (String) dbValue;
                        //内置 JSON 转为对象
                        if (column != null && column.json()) {
                            JsonSerializer jsonSerializer = Config.getConfig().getJsonSerializer();
                            Object decodeValue = jsonSerializer.fromJson(_dbValue, field.getType());
                            field.set(obj, decodeValue);
                            continue;
                        }
                    }
                    // 自定义转换器
                    Convert convert = field.getAnnotation(Convert.class);
                    if (convert != null && dbValue instanceof Serializable) {
                        Serializable _dbValue = (Serializable) dbValue;
                        Converter<?, Serializable> converter = Helper.getConverter(convert.converter());
                        field.set(obj, converter.recover(_dbValue));
                        continue;
                    }
                    // JDBC返回的数据类型与类属性类型一致, 直接设置属性值
                    if (dbValue.getClass() == field.getType() || field.getType().isInstance(dbValue)) {
                        field.set(obj, dbValue);
                        continue;
                    }
                    // JDBC返回的数据类型是String
                    if (field.getType() == String.class) {
                        entry.getValue().set(obj, dbValue.toString());
                        continue;
                    }
                    // 否则转换
                    ConversionService conversionService = Config.getConfig().getConversionService();
                    if (conversionService.canConvert(dbValue.getClass(), field.getType())) {
                        field.set(obj, conversionService.convert(dbValue, field.getType()));
                    } else {
                        logger.error("Cannot convert type {} to {}", dbValue.getClass(), field.getType());
                    }
                }
            }
            return obj;
        } catch (ReflectiveOperationException e) {
            throw new SqlteException(e);
        }
    }

    @Override
    public T map(Row row) {
        return copy(row, supplier.get());
    }
}