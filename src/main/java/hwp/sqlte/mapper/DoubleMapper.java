package hwp.sqlte.mapper;


import hwp.sqlte.Row;
import hwp.sqlte.RowMapper;

/**
 * @author Zero
 *         Created on 2017/3/20.
 */
public class DoubleMapper implements RowMapper<Double> {
    @Override
    public Double map(Row row) {
        Object v = row.values().iterator().next();
        if (v instanceof Double) {
            return (Double) v;
        }
        Number number = (Number) v;
        return number.doubleValue();
    }
}