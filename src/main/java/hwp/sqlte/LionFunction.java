package hwp.sqlte;

/**
 * @author Zero
 *         Created on 2017/3/27.
 */
@FunctionalInterface
public interface LionFunction<T, R>  {

    R apply(T t) throws Exception;
}
