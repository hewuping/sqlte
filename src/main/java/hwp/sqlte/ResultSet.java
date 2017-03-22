package hwp.sqlte;

import java.util.Collections;
import java.util.List;

/**
 * @author Zero
 *         Created on 2017/3/20.
 */
public class ResultSet {

    private List<String> culumns;
    private List<Row> rows;

    public ResultSet(List<String> culumns, List<Row> rows) {
        this.culumns = culumns;
        this.rows = rows;
    }

    public List<String> getCulumns() {
        return culumns;
    }

    public List<Row> getRows() {
        return rows;
    }

    public Row firstRow(){
        if (this.rows != null && this.rows.size() > 0) {
            return this.rows.get(0);
        }
        return null;
    }

    protected void unmodifiableRows(){
        this.rows = Collections.unmodifiableList(this.rows);
    }
}
