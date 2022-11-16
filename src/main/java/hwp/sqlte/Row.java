package hwp.sqlte;


import java.math.BigDecimal;
import java.sql.*;
import java.time.*;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author Zero
 * Created on 2017/3/20.
 */
public class Row extends HashMap<String, Object> {

    public String getString(String name) {
        return getValue(name, String.class);
    }

    public Long getLong(String name) {
        return getValue(name, Long.class);
    }

    public Integer getInteger(String name) {
        return getValue(name, Integer.class);
    }

    public BigDecimal getBigDecimal(String name) {
        return getValue(name, BigDecimal.class);
    }

    public Float getFloat(String name) {
        return getValue(name, Float.class);
    }

    public Double getDouble(String name) {
        return getValue(name, Double.class);
    }


    public LocalDate getLocalDate(String name) {
        return getValue(name, LocalDate.class);
    }

    public LocalTime getLocalTime(String name) {
        return getValue(name, LocalTime.class);
    }

    public LocalDateTime getLocalDateTime(String name) {
        return getValue(name, LocalDateTime.class);
    }

    public Timestamp getTimestamp(String name) {
        return getValue(name, Timestamp.class);
    }

    public Date getDate(String name) {
        return getValue(name, Date.class);
    }

    public Time getTime(String name) {
        return getValue(name, Time.class);
    }

    public OffsetDateTime getOffsetDateTime(String name) {
        return getValue(name, OffsetDateTime.class);
    }

    public ZonedDateTime getZonedDateTime(String name) {
        return getValue(name, ZonedDateTime.class);
    }

    public Instant getInstant(String name) {
        return getValue(name, Instant.class);
    }

    public Number getNumber(String name) {
        return (Number) get(name);
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(String name, T defValue) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(defValue);
        T v = (T) getValue(name, defValue.getClass());
        return v == null ? defValue : v;
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(String name) {
        Objects.requireNonNull(name);
        return (T) super.get(name);
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(String name, Class<T> tClass) {
        Objects.requireNonNull(name);
        Object value = get(name);
        if (value == null) return null;
        if (value.getClass() == tClass || tClass.isInstance(value)) {
            return (T) value;
        } else {
            ConversionService conversionService = Config.getConfig().getConversionService();
            return conversionService.convert(value, tClass);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> getOptValue(String name) {
        return Optional.ofNullable((T) super.get(name));
    }

    public <T> T map(RowMapper<T> mapper) {
        return mapper.map(this);
    }


    public <T> T map(Supplier<T> supplier) {
        return copyTo(supplier.get());
    }

    public <T> T map(Class<T> clazz) {
        try {
            return copyTo(clazz.getDeclaredConstructor().newInstance());
        } catch (Exception e) {
            throw new SqlteException(e);
        }
    }

    public <T> T copyTo(T bean) {
        return BeanMapper.copy(this, bean);
    }

    public Row set(String name, Object val) {
        put(name, val);
        return this;
    }

    public static Row from(ResultSet rs) {
        try {
            Row row = new Row();
            ResultSetMetaData metaData = rs.getMetaData();
            int cols = metaData.getColumnCount();
            for (int i = 1; i <= cols; i++) {
                row.put(metaData.getColumnLabel(i).intern(), rs.getObject(i));
            }
            return row;
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }


}
