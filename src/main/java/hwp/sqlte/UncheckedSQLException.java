package hwp.sqlte;

import java.sql.SQLException;

/**
 * @author Zero
 *         Created on 2018/3/27.
 */
public class UncheckedSQLException extends RuntimeException {
    public UncheckedSQLException(SQLException cause) {
        super(cause.getCause() == null ? cause : cause.getCause());
    }
}
