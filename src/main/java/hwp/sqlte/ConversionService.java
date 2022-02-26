package hwp.sqlte;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
            register(String.class, Byte.TYPE, new StringToByteConverter());
            register(String.class, Short.class, new StringToShortConverter());
            register(String.class, Short.TYPE, new StringToShortConverter());
            register(String.class, Integer.class, new StringToIntegerConverter());
            register(String.class, Integer.TYPE, new StringToIntegerConverter());
            register(String.class, Float.class, new StringToFloatConverter());
            register(String.class, Float.TYPE, new StringToFloatConverter());
            register(String.class, Double.class, new StringToDoubleConverter());
            register(String.class, Double.TYPE, new StringToDoubleConverter());
            register(String.class, Long.class, new StringToLongConverter());
            register(String.class, Long.TYPE, new StringToLongConverter());
            register(String.class, BigInteger.class, new StringToBigIntegerConverter());
            register(String.class, BigDecimal.class, new StringToBigDecimalConverter());
            register(String.class, Currency.class, new StringToCurrencyConverter());
            register(String.class, UUID.class, new StringToUUIDConverter());
            register(String.class, Boolean.class, new StringToBooleanConverter());
            register(String.class, Boolean.TYPE, new StringToBooleanConverter());

            //to String
            register(Object.class, String.class, Object::toString);

            // Boolean
            register(Boolean.class, Boolean.class, new BooleanToBoolean());
            register(Boolean.class, Boolean.TYPE, new BooleanToBoolean());
            register(Boolean.class, String.class, new BooleanToString());
            register(Boolean.class, Number.class, new BooleanToNumber());
            register(Boolean.class, Integer.class, new BooleanToInteger());
            register(Boolean.class, Integer.TYPE, new BooleanToInteger());
            register(Boolean.class, Long.class, new BooleanToLong());
            register(Boolean.class, Long.TYPE, new BooleanToLong());
            register(Boolean.class, Double.class, new BooleanToDouble());
            register(Boolean.class, Double.TYPE, new BooleanToDouble());
            register(Boolean.class, Float.class, new BooleanToFloat());
            register(Boolean.class, Float.TYPE, new BooleanToFloat());
            register(Boolean.class, Byte.class, new BooleanToByte());
            register(Boolean.class, Byte.TYPE, new BooleanToByte());

            register(Short.class, Boolean.TYPE, new NumberToBoolean<Short>());
            register(Short.class, Boolean.class, new NumberToBoolean<Short>());
            register(Integer.class, Boolean.TYPE, new NumberToBoolean<Integer>());
            register(Integer.class, Boolean.class, new NumberToBoolean<Integer>());
            register(Long.class, Boolean.TYPE, new NumberToBoolean<Long>());
            register(Long.class, Boolean.class, new NumberToBoolean<Long>());

            // number
            Class<?>[] numbers = new Class<?>[]{
                    Byte.TYPE, Short.TYPE, Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE,
                    Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, BigDecimal.class
            };
            for (Class<?> f : numbers) {
                for (Class<?> t : numbers) {
                    register(f, t, new NumberToNumberConverter(t));
                }
            }

            // date & time
            TimeZone timeZone = Config.getConfig().getDatabaseTimeZone();
//            ZoneOffset zoneOffset = OffsetDateTime.now().getOffset();

            register(Timestamp.class, String.class, timestamp -> timestamp.toInstant().toString());
            register(Timestamp.class, Long.class, Timestamp::getTime);
            register(Timestamp.class, LocalDateTime.class, Timestamp::toLocalDateTime);
            register(Timestamp.class, LocalDate.class, timestamp -> timestamp.toLocalDateTime().toLocalDate());
            register(Timestamp.class, java.util.Date.class, timestamp -> timestamp);

            register(Date.class, String.class, Date::toString);
            register(Date.class, Instant.class, Date::toInstant);
            register(Date.class, Long.class, java.util.Date::getTime);
            register(Date.class, LocalDate.class, Date::toLocalDate);
            register(Date.class, LocalDateTime.class, date -> date.toLocalDate().atTime(LocalTime.MIN));

            register(Time.class, String.class, Time::toString);
            register(Time.class, LocalTime.class, Time::toLocalTime);
            register(Time.class, Instant.class, Time::toInstant);

            register(LocalDateTime.class, Timestamp.class, Timestamp::valueOf);
            register(LocalDateTime.class, Date.class, dateTime -> Date.valueOf(dateTime.toLocalDate()));
            register(LocalDateTime.class, Long.class, dateTime -> dateTime.toEpochSecond(ZoneOffset.UTC) * 1000L);
            register(LocalDateTime.class, String.class, LocalDateTime::toString);
            register(LocalDateTime.class, java.util.Date.class, dateTime -> {
                Instant instant = dateTime.atZone(timeZone.toZoneId()).toInstant();
                return java.util.Date.from(instant);
            });

            register(LocalDate.class, String.class, LocalDate::toString);
            register(LocalDate.class, Long.class, date -> date.atStartOfDay(timeZone.toZoneId()).getSecond() * 1000L);
            register(LocalDate.class, Integer.class, date -> date.atStartOfDay(timeZone.toZoneId()).getSecond());
            register(LocalDate.class, java.util.Date.class, date -> {
                Instant instant = date.atStartOfDay(timeZone.toZoneId()).toInstant();
                return java.util.Date.from(instant);
            });

            register(OffsetDateTime.class, String.class, OffsetDateTime::toString);
            register(OffsetDateTime.class, Date.class, dateTime -> Date.valueOf(dateTime.toLocalDate()));
            register(OffsetDateTime.class, java.util.Date.class, dateTime -> java.util.Date.from(dateTime.toZonedDateTime().toInstant()));
            register(OffsetDateTime.class, Long.class, dateTime -> dateTime.toZonedDateTime().toInstant().getEpochSecond() * 1000L);

            register(ZonedDateTime.class, String.class, ZonedDateTime::toString);
            register(ZonedDateTime.class, Date.class, dateTime -> new Date(dateTime.toEpochSecond() * 1000L));
            register(ZonedDateTime.class, java.util.Date.class, dateTime -> java.util.Date.from(dateTime.toInstant()));
            register(ZonedDateTime.class, Long.class, dateTime -> dateTime.toEpochSecond() * 1000L);

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
            if (from == null && to == Boolean.TYPE) {
                return (T) Boolean.FALSE;
            }
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
                T[] items = to.getEnumConstants();
                for (T item : items) {
                    Enum e = (Enum) item;
                    if (e.name().equalsIgnoreCase(_from)) {
                        return item;
                    }
                }
                return (T) Enum.valueOf((Class) to, _from.toUpperCase());
            }
            if (from instanceof Enum && String.class == to) {
                return (T) ((Enum) from).name();
            }
            Map<Class<?>, TypeConverter<Object, Object>> map1 = map.get(from.getClass());
            if (map1 == null) {
                throw new IllegalArgumentException("Conversion not supported: " + from.getClass() + " -> " + to);
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
            return Byte.valueOf(s);
        }
    }

    class StringToShortConverter implements TypeConverter<String, Short> {
        @Override
        public Short convert(String s) {
            return Short.valueOf(s);
        }
    }

    class StringToIntegerConverter implements TypeConverter<String, Integer> {
        @Override
        public Integer convert(String s) {
            return Integer.valueOf(s);
        }
    }

    class StringToLongConverter implements TypeConverter<String, Long> {
        @Override
        public Long convert(String s) {
            return Long.valueOf(s);
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
            return Float.valueOf(s);
        }
    }

    class StringToDoubleConverter implements TypeConverter<String, Double> {
        @Override
        public Double convert(String s) {
            return Double.valueOf(s);
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

    final class StringToBooleanConverter implements TypeConverter<String, Boolean> {
        public Boolean convert(String source) {
            if (source == null || source.isEmpty()) {
                return null;
            }
            return ("y".equalsIgnoreCase(source) || "true".equalsIgnoreCase(source) || "1".equalsIgnoreCase(source));
        }
    }


    final class BooleanToBoolean implements TypeConverter<Boolean, Boolean> {
        public Boolean convert(Boolean source) {
            return source;
        }
    }

    final class BooleanToInteger implements TypeConverter<Boolean, Integer> {
        public Integer convert(Boolean source) {
            return source ? 1 : 0;
        }
    }

    final class BooleanToLong implements TypeConverter<Boolean, Long> {
        public Long convert(Boolean source) {
            return source ? 1L : 0;
        }
    }

    final class BooleanToNumber implements TypeConverter<Boolean, Number> {
        public Number convert(Boolean source) {
            return (Number) (source ? 1 : 0);
        }
    }

    final class BooleanToDouble implements TypeConverter<Boolean, Double> {
        public Double convert(Boolean source) {
            return source ? 1D : 0;
        }
    }

    final class BooleanToFloat implements TypeConverter<Boolean, Float> {
        public Float convert(Boolean source) {
            return source ? 1F : 0;
        }
    }

    final class BooleanToByte implements TypeConverter<Boolean, Byte> {
        public Byte convert(Boolean source) {
            return (byte) (source ? 1 : 0);
        }
    }

    final class BooleanToString implements TypeConverter<Boolean, String> {
        public String convert(Boolean source) {
            return source ? "true" : "false";
        }
    }

    final class NumberToBoolean<T extends Number> implements TypeConverter<T, Boolean> {
        public Boolean convert(T source) {
            return source.intValue() == 0 ? Boolean.FALSE : Boolean.TRUE;
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
//                return toClass.cast(o.intValue());
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


    final class TimestampToDateTimeConverter implements TypeConverter<Timestamp, java.util.Date> {
        public java.util.Date convert(Timestamp source) {
            return new java.util.Date(source.getTime());
        }
    }


}