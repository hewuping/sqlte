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
        return (Double) row.values().iterator().next();
    }
}