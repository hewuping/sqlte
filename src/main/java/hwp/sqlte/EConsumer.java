package hwp.sqlte;

/**
 * @author Zero
 * Created on 2021/4/21.
 */
@FunctionalInterface
public interface EConsumer<T> {
    void accept(T t) throws Exception;
}
