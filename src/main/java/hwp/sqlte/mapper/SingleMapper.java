package hwp.sqlte.mapper;


import hwp.sqlte.Row;
import hwp.sqlte.RowMapper;

/**
 * @author Zero
 *         Created on 2017/3/20.
 */
public class SingleMapper<T> implements RowMapper<T> {
    @Override
    public T map(Row row) {
        return (T) row.values().iterator().next();
    }
}