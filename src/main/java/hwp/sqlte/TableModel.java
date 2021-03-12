package hwp.sqlte;

import java.util.List;

/**
 * @author Zero
 * Created on 2021/1/12.
 */
public class TableModel<T> {
    private final List<T> data;
    private final long rowCount;
    private Integer page;
    private Integer pageSize;

    public TableModel(List<T> data, long rowCount) {
        this.data = data;
        this.rowCount = rowCount;
    }

    public TableModel(List<T> data, long rowCount, Integer page, Integer pageSize) {
        this.data = data;
        this.rowCount = rowCount;
        this.pageSize = pageSize;
        this.page = page;
    }

    public List<T> getData() {
        return data;
    }

    public long getRowCount() {
        return rowCount;
    }

}
