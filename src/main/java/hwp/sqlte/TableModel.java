package hwp.sqlte;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    public Map<String, Object> asMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("data", data);
        map.put("rowCount", rowCount);
        if (page != null) {
            map.put("page", page);
        }
        if (pageSize != null) {
            map.put("pageSize", pageSize);
        }
        return map;
    }

}
