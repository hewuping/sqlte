package hwp.sqlte;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author Zero
 * Created on 2021/7/7.
 */
public class DataList<T> {
    private final List<T> list;
    private final long count;

    public DataList(List<T> list, long count) {
        this.list = list;
        this.count = count;
    }

    public List<T> getList() {
        return list;
    }

    public long getCount() {
        return count;
    }

    public void forEach(Consumer<? super T> consumer) {
        if (list != null) {
            list.forEach(consumer);
        }
    }

    public TableModel<T> asTableModel(int page, int pageSize) {
        return new TableModel<>(list, count, page, pageSize);
    }
}
