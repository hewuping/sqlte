package hwp.sqlte;

import java.io.Serializable;
import java.util.Objects;

/**
 * 范围查找: <code>BETWEEN ? AND ?</code>
 *
 * @param <T>
 */
public final class Range<T> implements Serializable {

    private T start;
    private T end;


    public Range() {
    }

    public Range(T start, T to) {
        this.start = start;
        this.end = to;
    }

    public static <T> Range<T> of(T start, T end) {
        return new Range<>(start, end);
    }


    public T getStart() {
        return start;
    }

    public T getStartDefault(T def) {
        Objects.requireNonNull(def, "默认值不能为NULL");
        return start == null ? def : start;
    }

    public void setStart(T start) {
        this.start = start;
    }

    public T getEnd() {
        return end;
    }

    public T getEndDefault(T def) {
        Objects.requireNonNull(def, "默认值不能为NULL");
        return end == null ? def : end;
    }

    public void setEnd(T end) {
        this.end = end;
    }

    @Override
    public String toString() {
        return "{start=" + start + ", end=" + end + '}';
    }
}
