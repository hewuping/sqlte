package hwp.sqlte;

/**
 * @author Zero
 * Created on 2018/5/7.
 */
public interface RowHandler {
    /**
     * @param row
     * @return
     */
    boolean handle(Row row);
}
