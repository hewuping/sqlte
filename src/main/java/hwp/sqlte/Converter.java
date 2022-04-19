package hwp.sqlte;

import java.io.Serializable;

/**
 * @author Zero
 * Created on 2022/2/10.
 */
public interface Converter<T, D extends Serializable> {

    /**
     *
     * @param value
     * @return
     */
    public D convert(T value);

    public T recover(D dbValue);

}
