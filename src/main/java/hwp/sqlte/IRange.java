package hwp.sqlte;

/**
 * 新增 IRange 接口, 方便适配和扩展
 *
 * @param <T>
 */
public interface IRange<T> {

    T getStart();

    T getEnd();

}
