package hwp.sqlte;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
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
            Class<?>[] numbers = new Class<?>[]{
                    Byte.TYPE, Short.TYPE, Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE,
                    Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class
            };
            for (Class<?> f : numbers) {
                for (Class<?> t : numbers) {
                    register(f, t, new NumberToNumberConverter(t));
                }
            }

            //time
            register(Time.class, Long.class, new TimeToLongConverter());
            register(Time.class, LocalTime.class, new TimeToLocalTimeConverter());
            register(Timestamp.class, Long.class, new TimestampToLongConverter());
            register(Timestamp.class, LocalDateTime.class, new TimestampToLocalDateTimeConverter());
            register(Timestamp.class, LocalDate.class, new TimestampToLocalDateConverter());
            register(Timestamp.class, java.util.Date.class, new TimestampToDateTimeConverter());
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
            if (from == null) return null;
            if (from instanceof Integer && to.isEnum()) {
                return to.getEnumConstants()[(Integer) from];
            }
            if (from instanceof Enum && Integer.class == to) {
                return (T) Integer.valueOf(((Enum) from).ordinal());
            }
            if (from instanceof String && to.isEnum()) {
                String _from = (String) from;
                if (_from.isEmpty()) {
                    return null;
                }
                return (T) Enum.valueOf((Class) to, _from.toUpperCase());
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
            return (T) from;
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
            return ZonedDateTime.of(source.toLocalDateTime(), ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);//2018-11-30T16:08:37.734Z
        }
    }

    final class DateToStringConverter implements TypeConverter<Date, String> {
        public String convert(Date source) {
//            return ZonedDateTime.of(source.toLocalDate().atStartOfDay(), ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE);
             return DateTimeFormatter.ofPattern("yyyy-MM-dd").format(source.toLocalDate());
        }
    }

    final class TimeToStringConverter implements TypeConverter<Time, String> {
        public String convert(Time source) {
            return DateTimeFormatter.ofPattern("HH:mm:ss").format(source.toLocalTime());
        }
    }

    final class TimeToLocalTimeConverter implements TypeConverter<Time, LocalTime> {
        public LocalTime convert(Time source) {
            return source.toLocalTime();
        }
    }

    @SuppressWarnings("unchecked")
    final class NumberToNumberConverter<F extends Number, T extends Number> implements TypeConverter<F, T> {
        private Class<T> toClass;

        NumberToNumberConverter(Class<T> toClass) {
            this.toClass = toClass;
        }

        public T convert(F o) {
            if (toClass == o.getClass()) {
                return (T) o;
            }
            if (toClass == Integer.TYPE || toClass == Integer.class) {
                return (T) ((Integer) o.intValue());
            }
            if (toClass == Long.TYPE || toClass == Long.class) {
                return (T) ((Long) o.longValue());
            }
            if (toClass == Short.TYPE || toClass == Short.class) {
                return (T) ((Short) o.shortValue());
            }
            if (toClass == Float.TYPE || toClass == Float.class) {
                return (T) ((Float) o.floatValue());
            }
            if (toClass == Double.TYPE || toClass == Double.class) {
                return (T) ((Double) o.doubleValue());
            }
            if (toClass == Byte.TYPE || toClass == Byte.class) {
                return (T) ((Byte) o.byteValue());
            }
            return (T) o;
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

    final class TimestampToDateTimeConverter implements TypeConverter<Timestamp, java.util.Date> {
        public java.util.Date convert(Timestamp source) {
            return new java.util.Date(source.getTime());
        }
    }

    final class DateToLocalDateConverter implements TypeConverter<Date, LocalDate> {
        public LocalDate convert(Date source) {
            return source.toLocalDate();
        }
    }

}