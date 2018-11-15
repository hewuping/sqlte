package hwp.sqlte;

/**
 * @author Zero
 * Created on 2018/3/27.
 */
public class UncheckedSQLException extends RuntimeException {
    public UncheckedSQLException(Throwable cause) {
        super(cause.getCause() == null ? cause : cause.getCause());
    }
}
