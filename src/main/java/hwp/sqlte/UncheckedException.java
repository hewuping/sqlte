package hwp.sqlte;

/**
 * @author Zero
 * Created on 2018/3/27.
 */
public class UncheckedException extends RuntimeException {
    public UncheckedException(Throwable cause) {
        super(cause);
    }

    public UncheckedException(String message) {
        super(message);
    }

    public UncheckedException(String message, Throwable cause) {
        super(message, cause);
    }
}
