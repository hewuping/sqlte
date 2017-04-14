package hwp.sqlte;

import java.util.Arrays;

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

    default String id() {
        return sql().concat("@").concat(Arrays.toString(args()));
    }

}
