package hwp.sqlte.mapper;


import hwp.sqlte.Row;
import hwp.sqlte.RowMapper;

/**
 * @author Zero
 *         Created on 2017/3/20.
 */
public class IntMapper implements RowMapper<Integer> {

    public static final IntMapper MAPPER = new IntMapper();

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