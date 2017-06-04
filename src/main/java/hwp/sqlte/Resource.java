package hwp.sqlte;

/**
 * @author Zero
 *         Created by Zero on 2017/6/4 0004.
 */
public class Resource<T> {

    private T obj;

    public T get() {
        return obj;
    }

    public void set(T t) {
        obj = t;
    }


}
