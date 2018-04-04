package hwp.sqlte;

import java.sql.SQLException;

/**
 * @author Zero
 *         Created on 2018/3/27.
 */
public class UncheckedException extends RuntimeException {
    public UncheckedException(SQLException cause) {
        super(cause);
    }
}
