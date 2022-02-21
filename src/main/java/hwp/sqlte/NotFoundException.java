package hwp.sqlte;

/**
 * @author Zero
 * Created on 2018/3/27.
 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(Throwable cause) {
        super(cause.getCause() == null ? cause : cause.getCause());
    }
}
