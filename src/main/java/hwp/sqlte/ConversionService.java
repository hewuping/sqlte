package hwp.sqlte;

public interface ConversionService {

    static ConversionService getDefault() {
        return DefaultConversionService.INSTANCE;
    }

    boolean canConvert(Class<?> from, Class<?> to);

    <T> T convert(Object from, Class<T> to);

    <F, T> void register(Class<F> from, Class<T> to, TypeConverter<F, T> converter);

    interface TypeConverter<F, T> {
        T convert(F f);
    }

}