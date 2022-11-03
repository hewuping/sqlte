package hwp.sqlte;

public interface ConversionService {

    static ConversionService getDefault() {
        return DefaultConversionService.INSTANCE;
    }

    /**
     * 检测是否支持类型转换
     *
     * @param from
     * @param to
     * @return
     */
    boolean canConvert(Class<?> from, Class<?> to);

    /**
     * 类型转换
     * <pre>{@code
     *  // 基础数据类型转换
     *  service.convert("123", Integer.class);
     *  service.convert("123", Long.class);
     *  ...
     *
     *  // 日期时间转换
     *  convert("12:10:08", LocalTime.class);
     *  convert(new Date(), LocalDate.class);
     *  convert(new Date(), LocalDateTime.class);
     *  ...
     *
     *  // 数组转换
     *  String[] strings = new String[]{"123", "456", "789"};
     *  convert(strings, Long[].class)
     *  convert(strings, Integer[].class)
     *
     *  // enum 转换
     *  convert("SECONDS", TimeUnit.class)
     *
     * } </pre>
     *
     * @param from
     * @param to
     * @param <T>
     * @return
     */
    <T> T convert(Object from, Class<T> to);

    /**
     * 注册转换器
     *
     * @param from
     * @param to
     * @param converter
     * @param <F>
     * @param <T>
     */
    <F, T> void register(Class<F> from, Class<T> to, TypeConverter<F, T> converter);

    interface TypeConverter<F, T> {
        T convert(F f);
    }

}