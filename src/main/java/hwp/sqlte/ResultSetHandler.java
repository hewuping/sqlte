package hwp.sqlte;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Zero
 * Created on 2020/8/6.
 */
public interface ResultSetHandler {
    void accept(ResultSet rs) throws SQLException;
}
