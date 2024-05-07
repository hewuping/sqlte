package hwp.sqlte;

import java.util.Objects;

/**
 * 范围查询包括 start 和 end，意思等同 SQL BETWEEN 中的 begin 和 end。
 * <p>
 * 生成 SQL “{@code BETWEEN ? AND ?}”，查询区间为: [start, end]
 *
 * @param <T>
 */
public final class Range<T> implements IRange<T> {

    private T start;
    private T end;


    public Range() {
    }

    private Range(T start, T end) {
        this.start = start;
        this.end = end;
    }

    public static <T> Range<T> of(T start, T end) {
        return new Range<>(start, end);
    }


    public T getStart() {
        return start;
    }

    /**
     * 获取 start, 如果 end 为null 则使用默认值
     *
     * @param def 默认值
     * @return
     */
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

    /**
     * 获取 end, 如果 end 为null 则使用默认值
     *
     * @param def 默认值
     * @return
     */
    public T getEnd(T def) {
        Objects.requireNonNull(def, "默认值不能为 NULL");
        return end == null ? def : end;
    }

    public void setEnd(T end) {
        this.end = end;
    }

    /**
     * start 和 end 是否都为空(null和空字符串都认为是空)
     *
     * @return
     */
    public boolean isEmpty() {
        return (start == null || "".equals(start)) && (end == null || "".equals(end));
    }

    /**
     * start 和 end 是都不为空
     *
     * @return
     */
    public boolean isNotEmpty() {
        return !isEmpty();
    }

    @Override
    public String toString() {
        return "{start=" + start + ", end=" + end + '}';
    }
}
