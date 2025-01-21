package hwp.sqlte;

/**
 * @author Zero
 * Created on 2024/8/23.
 */
public class WhereClauseException extends SqlteException {

    public WhereClauseException(Throwable cause) {
        super(cause);
    }

    public WhereClauseException(String msg) {
        super(msg);
    }

}
