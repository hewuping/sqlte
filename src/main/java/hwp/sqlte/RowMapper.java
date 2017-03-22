package hwp.sqlte;


/**
 * @author Zero
 *         Created on 2017/3/20.
 */
public interface RowMapper<T> {

    T map(Row row);

}
