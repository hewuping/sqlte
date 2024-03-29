package hwp.sqlte;

import java.io.Serializable;
import java.util.Objects;

/**
 * 范围查询包括 start 和 end，意思等同 SQL BETWEEN 中的 begin 和 end。
 * <p>
 * 生成 SQL “{@code BETWEEN ? AND ?}”，查询区间为: [start, end]
 *
 * @param <T>
 */
public final class Range<T> implements Serializable {

    private T start;
    private T end;


    public Range() {
    }

    private Range(T start, T to) {
        this.start = start;
        this.end = to;
    }

    public static <T> Range<T> of(T start, T end) {
        return new Range<>(start, end);
    }


    public T getStart() {
        return start;
    }

    public T getStart(T def) {
        Objects.requireNonNull(def, "默认值不能为 NULL");
        return start == null ? def : start;
    }

    public void setStart(T start) {
        this.start = start;
    }

    public T getEnd() {
        return end;
    }

    public T getEnd(T def) {
        Objects.requireNonNull(def, "默认值不能为 NULL");
        return end == null ? def : end;
    }

    public void setEnd(T end) {
        this.end = end;
    }

    public boolean isEmpty() {
        return (start == null || "".equals(start)) && (end == null || "".equals(end));
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }

    @Override
    public String toString() {
        return "{start=" + start + ", end=" + end + '}';
    }
}
