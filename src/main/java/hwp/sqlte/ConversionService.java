package hwp.sqlte;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public interface ConversionService {

    boolean canConvert(Class<?> from, Class<?> to);

    <T> T convert(Object from, Class<T> to);

    <F, T> void register(Class<F> from, Class<T> to, TypeConverter<F, T> converter);

    interface TypeConverter<F, T> {
        T convert(F f);
    }

    ConversionService DEFAULT = new DefaultConversionService();

    final class DefaultConversionService implements ConversionService {

        private Map<Class<?>, Map<Class<?>, TypeConverter<Object, Object>>> map = new HashMap<>();

        public DefaultConversionService() {
            //String to X
            register(String.class, Character.class, new StringToCharacterConverter());
            register(String.class, Byte.class, new StringToByteConverter());
            register(String.class, Short.class, new StringToShortConverter());
            register(String.class, Integer.class, new StringToIntegerConverter());
            register(String.class, Float.class, new StringToFloatConverter());
            register(String.class, Double.class, new StringToDoubleConverter());
            register(String.class, Long.class, new StringToLongConverter());
            register(String.class, BigInteger.class, new StringToBigIntegerConverter());
            register(String.class, BigDecimal.class, new StringToBigDecimalConverter());
            register(String.class, Currency.class, new StringToCurrencyConverter());
            register(String.class, UUID.class, new StringToUUIDConverter());

            //to String
            register(Object.class, String.class, new ObjectToStringConverter());
            register(Timestamp.class, String.class, new TimestampToStringConverter());
            register(Time.class, String.class, new TimeToStringConverter());
            register(Date.class, String.class, new DateToStringConverter());
            //int
            register(Long.class, Integer.class, new LongToIntegerConverter());
            register(Integer.class, Long.class, new IntegerToLongConverter());
            //float
            register(Double.class, Float.class, new DoubleToFloatConverter());
            register(Float.class, Double.class, new FloatToDoubleConverter());
            //time
            register(Time.class, Long.class, new TimeToLongConverter());
            register(Time.class, LocalTime.class, new TimeToLocalTimeConverter());
            register(Timestamp.class, Long.class, new TimestampToLongConverter());
            register(Timestamp.class, LocalDate.class, new TimestampToLocalDateConverter());
            register(Timestamp.class, LocalDateTime.class, new TimestampToLocalDateTimeConverter());
            register(Date.class, Long.class, new DateToLongConverter());
            register(Date.class, LocalDate.class, new DateToLocalDateConverter());
            //
//            register(Integer.class, Enum.class, new IntegerToEnumConverter());
        }

        @Override
        public boolean canConvert(Class<?> from, Class<?> to) {
            if (Integer.class == from && to.isEnum()) {
                return true;
            }
            if (from.isEnum() && Integer.class == to) {
                return true;
            }
            if (String.class == from && to.isEnum()) {
                return true;
            }
            if (from.isEnum() && String.class == to) {
                return true;
            }
            Map<Class<?>, TypeConverter<Object, Object>> map1 = map.get(from);
            if (map1 == null) {
                return false;
            }
            return map1.containsKey(to);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T convert(Object from, Class<T> to) {
            if (from instanceof Integer && to.isEnum()) {
                return to.getEnumConstants()[(Integer) from];
            }
            if (from instanceof Enum && Integer.class == to) {
                return (T) Integer.valueOf(((Enum) from).ordinal());
            }
            if (from instanceof String && to.isEnum()) {
                return (T) Enum.valueOf((Class) to, (String) from);
            }
            if (from instanceof Enum && String.class == to) {
                return (T) ((Enum) from).name();
            }
            Map<Class<?>, TypeConverter<Object, Object>> map1 = map.get(from.getClass());
            if (map1 == null) {
                return null;
            }
            TypeConverter<Object, Object> converter = map1.get(to);
            if (converter != null) {
                return (T) converter.convert(from);
            }
            return null;
        }


        @Override
        public <F, T> void register(Class<F> from, Class<T> to, TypeConverter<F, T> converter) {
            Map<Class<?>, TypeConverter<Object, Object>> map1 = map.computeIfAbsent(from, k -> new HashMap<>());
            map1.put(to, (TypeConverter<Object, Object>) converter);
        }


    }

    final class StringToCharacterConverter implements TypeConverter<String, Character> {
        public Character convert(String source) {
            if (source.length() == 0) {
                return null;
            } else if (source.length() > 1) {
                throw new IllegalArgumentException("Can only convert a [String] with length of 1 to a [Character]; string value '" + source + "'  has length of " + source.length());
            } else {
                return source.charAt(0);
            }
        }
    }

    class StringToByteConverter implements TypeConverter<String, Byte> {
        @Override
        public Byte convert(String s) {
            return Byte.parseByte(s);
        }
    }

    class StringToShortConverter implements TypeConverter<String, Short> {
        @Override
        public Short convert(String s) {
            return Short.parseShort(s);
        }
    }

    class StringToIntegerConverter implements TypeConverter<String, Integer> {
        @Override
        public Integer convert(String s) {
            return Integer.parseInt(s);
        }
    }

    class StringToLongConverter implements TypeConverter<String, Long> {
        @Override
        public Long convert(String s) {
            return Long.parseLong(s);
        }
    }

    class StringToBigIntegerConverter implements TypeConverter<String, BigInteger> {
        @Override
        public BigInteger convert(String s) {
            return new BigInteger(s);
        }
    }

    class StringToBigDecimalConverter implements TypeConverter<String, BigDecimal> {
        @Override
        public BigDecimal convert(String s) {
            return new BigDecimal(s);
        }
    }

    class StringToFloatConverter implements TypeConverter<String, Float> {
        @Override
        public Float convert(String s) {
            return Float.parseFloat(s);
        }
    }

    class StringToDoubleConverter implements TypeConverter<String, Double> {
        @Override
        public Double convert(String s) {
            return Double.parseDouble(s);
        }
    }

    class StringToCurrencyConverter implements TypeConverter<String, Currency> {
        public Currency convert(String source) {
            return Currency.getInstance(source);
        }
    }


    final class StringToUUIDConverter implements TypeConverter<String, UUID> {
        public UUID convert(String source) {
            return source.length() > 0 ? UUID.fromString(source.trim()) : null;
        }
    }

    final class ObjectToStringConverter implements TypeConverter<Object, String> {
        public String convert(Object source) {
            return source.toString();
        }
    }

    final class TimestampToStringConverter implements TypeConverter<Timestamp, String> {
        public String convert(Timestamp source) {
            return ZonedDateTime.of(source.toLocalDateTime(), ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
        }
    }

    final class DateToStringConverter implements TypeConverter<Date, String> {
        public String convert(Date source) {
            return ZonedDateTime.of(source.toLocalDate().atStartOfDay(), ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
        }
    }

    final class TimeToStringConverter implements TypeConverter<Time, String> {
        public String convert(Time source) {
//            return ZonedDateTime.of(source.toString(), ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
            return source.toString();
        }
    }

    final class TimeToLocalTimeConverter implements TypeConverter<Time, LocalTime> {
        public LocalTime convert(Time source) {
            return source.toLocalTime();
        }
    }

    final class LongToIntegerConverter implements TypeConverter<Long, Integer> {
        public Integer convert(Long source) {
            return source.intValue();
        }
    }

    final class IntegerToLongConverter implements TypeConverter<Integer, Long> {
        public Long convert(Integer source) {
            return source.longValue();
        }
    }

    final class DoubleToFloatConverter implements TypeConverter<Double, Float> {
        public Float convert(Double source) {
            return source.floatValue();
        }
    }


    final class FloatToDoubleConverter implements TypeConverter<Float, Double> {
        public Double convert(Float source) {
            return source.doubleValue();
        }
    }

    final class TimeToLongConverter implements TypeConverter<Time, Long> {
        public Long convert(Time source) {
            return source.getTime();
        }
    }

    final class TimestampToLongConverter implements TypeConverter<Timestamp, Long> {
        public Long convert(Timestamp source) {
            return source.getTime();
        }
    }

    final class DateToLongConverter implements TypeConverter<Date, Long> {
        public Long convert(Date source) {
            return source.getTime();
        }
    }


    final class TimestampToLocalDateConverter implements TypeConverter<Timestamp, LocalDate> {
        public LocalDate convert(Timestamp source) {
            return source.toLocalDateTime().toLocalDate();
        }
    }

    final class TimestampToLocalDateTimeConverter implements TypeConverter<Timestamp, LocalDateTime> {
        public LocalDateTime convert(Timestamp source) {
            return source.toLocalDateTime();
        }
    }

    final class DateToLocalDateConverter implements TypeConverter<Date, LocalDate> {
        public LocalDate convert(Date source) {
            return source.toLocalDate();
        }
    }

}