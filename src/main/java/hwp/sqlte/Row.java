package hwp.sqlte;

import java.sql.SQLException;
import java.util.HashMap;

/**
 * @author Zero
 *         Created on 2017/3/20.
 */
public class Row extends HashMap<String, Object> {

    public String getString(String name) {
        return (String) get(name);
    }

    public <T> T val(String name) {
        return (T) get(name);
    }

    public <T> T map(RowMapper<T> mapper) throws SQLException {
        return mapper.map(this);
    }

}
