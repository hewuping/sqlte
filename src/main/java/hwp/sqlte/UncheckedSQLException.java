package hwp.sqlte;

/**
 * @author Zero
 * Created on 2018/3/27.
 */
public class UncheckedSQLException extends SqlteException {
    public UncheckedSQLException(String message) {
        super(message);
    }

    public UncheckedSQLException(Throwable cause) {
        super(cause.getCause() == null ? cause : cause.getCause());
    }
}
