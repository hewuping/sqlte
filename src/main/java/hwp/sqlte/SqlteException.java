package hwp.sqlte;

/**
 * @author Zero
 * Created on 2018/3/27.
 */
public class SqlteException extends RuntimeException {

    public static SqlteException warp(Exception e) {
        if (e instanceof SqlteException) {
            return (SqlteException) e;
        }
        return new SqlteException(e);
    }

    public SqlteException(Throwable cause) {
        super(cause);
    }

    public SqlteException(String msg) {
        super(msg);
    }
}
