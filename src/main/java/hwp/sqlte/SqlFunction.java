package hwp.sqlte;

import java.util.Objects;

/**
 * @author Zero
 *         Created by Zero on 2017/6/4 0004.
 */
public interface SqlFunction<T, R> {

    R apply(T t) throws Exception;

//    default <V> SqlFunction<T, V> andThen(SqlFunction<? super R, ? extends V> after) {
//        Objects.requireNonNull(after);
//        return (T t) -> after.apply(apply(t));
//    }

//    default SqlFunction<T, R> before(SqlFunction<T,R> before) {
//        Objects.requireNonNull(before);
//        return before::apply;
//    }

//    R before(T t) throws Exception;

}
