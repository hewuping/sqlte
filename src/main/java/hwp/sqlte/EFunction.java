package hwp.sqlte;

@FunctionalInterface
public interface EFunction<T, R> {

    R apply(T t) throws Exception;

}