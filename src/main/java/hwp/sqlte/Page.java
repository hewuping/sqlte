package hwp.sqlte;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Zero
 * Created on 2021/7/7.
 */
public class Page<T> {
    private final List<T> data;
    private final long rowCount;
    private Integer page;
    private Integer pageSize;

    public Page(List<T> data, long rowCount) {
        this.data = data;
        this.rowCount = rowCount;
    }

    public Page(List<T> data, long rowCount, Integer page, Integer pageSize) {
        this.data = data;
        this.rowCount = rowCount;
        this.page = page;
        this.pageSize = pageSize;
    }

    public List<T> getData() {
        return data;
    }

    public long getRowCount() {
        return rowCount;
    }

    public void forEach(Consumer<? super T> consumer) {
        if (data != null) {
            data.forEach(consumer);
        }
    }

    public Page<T> set(int page, int pageSize) {
        this.page = page;
        this.pageSize = pageSize;
        return this;
    }

    public <R> Page<R> map(Function<? super T, ? extends R> mapper) {
        List<R> list = this.data.stream().map(mapper).collect(Collectors.toList());
        return new Page<R>(list, rowCount);
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
