package hwp.sqlte;


import java.util.function.Function;

/**
 * @author Zero
 *         Created on 2017/3/20.
 */
public interface RowMapper<T> extends Function<Row,T> {

    T map(Row row);

    default T apply(Row row) {
        return map(row);
    }

}
