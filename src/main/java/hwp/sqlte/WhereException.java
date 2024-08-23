package hwp.sqlte;

/**
 * @author Zero
 * Created on 2024/8/23.
 */
public class WhereException extends SqlteException {

    public WhereException(Throwable cause) {
        super(cause);
    }

    public WhereException(String msg) {
        super(msg);
    }

}
