package hwp.sqlte;

/**
 * @author Zero
 * Created on 2018/3/27.
 */
public class SqlteException extends RuntimeException {
    public SqlteException(Throwable cause) {
        super(cause);
    }

    public SqlteException(String msg) {
        super(msg);
    }
}
