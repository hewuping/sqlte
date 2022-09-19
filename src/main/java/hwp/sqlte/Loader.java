package hwp.sqlte;

import java.util.function.Consumer;

public interface Loader<T> extends Consumer<Consumer<T>> {

    void load(Consumer<T> consumer);

    @Override
    default void accept(Consumer<T> consumer) {
        load(consumer);
    }

}
