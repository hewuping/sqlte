package hwp.sqlte.mapper;


import hwp.sqlte.Row;
import hwp.sqlte.RowMapper;

/**
 * @author Zero
 *         Created on 2017/3/20.
 */
public class NumberMapper implements RowMapper<Number> {
    @Override
    public Number map(Row row) {
        return (Number) row.values().iterator().next();
    }
}