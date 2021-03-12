package hwp.sqlte;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Example:
 * <pre>
 * BatchAction<Integer> action = new BatchAction<>(10, System.out::println);
 * for (int i = 1; i <= 37; i++) {
 *    action.add(i);
 * }
 * action.end();
 * </pre>
 * Out:
 * <pre>
 * [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
 * [11, 12, 13, 14, 15, 16, 17, 18, 19, 20]
 * [21, 22, 23, 24, 25, 26, 27, 28, 29, 30]
 * [31, 32, 33, 34, 35, 36, 37]
 * </pre>
 *
 * @author Zero
 */
public class BatchAction<T> {


    private final int batchSize;
    private Consumer<List<T>> action;

    private List<T> list;

    public BatchAction(int batchSize, Consumer<List<T>> action) {
        if (batchSize < 1) {
            throw new IllegalArgumentException("batchSize must be > 0");
        }
        Objects.requireNonNull(action, "action must be not null");
        this.batchSize = batchSize;
        this.action = action;
        this.list = new ArrayList<>(batchSize);
    }

    public void add(T t) {
        if (list == null) {
            throw new UnsupportedOperationException("Already called end()");
        }
        list.add(t);
        if (list.size() >= batchSize) {
            action.accept(list);
            list = new ArrayList<>(batchSize);
        }
    }

    public void end() {
        if (!list.isEmpty()) {
            action.accept(list);
            list = null;
        }
    }

}
