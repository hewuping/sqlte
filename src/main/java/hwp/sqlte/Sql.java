package hwp.sqlte;

/**
 * @author Zero
 *         Created on 2017/3/22.
 */
public interface Sql {

    /**
     * Get standard sql
     *
     * @return sql
     */
    String sql();

    /**
     * Get args
     *
     * @return
     */
    Object[] args();

}
