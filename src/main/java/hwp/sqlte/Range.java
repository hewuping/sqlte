package hwp.sqlte;

import java.io.Serializable;
import java.util.Objects;

/**
 * 值范围包括 start 和 end , 意思等同 SQL BETWEEN 中的 begin 和 end.
 * <p>
 * 转换为SQL时如: <code>BETWEEN ? AND ?</code>
 *
 * @param <T>
 */
public class Range<T> implements Serializable {

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

    @Override
    public String toString() {
        return "{start=" + start + ", end=" + end + '}';
    }
}
