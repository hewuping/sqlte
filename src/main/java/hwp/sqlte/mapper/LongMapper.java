package hwp.sqlte.mapper;


import hwp.sqlte.Row;
import hwp.sqlte.RowMapper;

/**
 * @author Zero
 *         Created on 2017/3/20.
 */
public class LongMapper implements RowMapper<Long> {

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