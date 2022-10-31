package hwp.sqlte;

import java.util.function.Consumer;

public interface Sender<T> extends Consumer<T> {

    void send(T obj);

    /**
     * 同 send()
     *
     * @param obj the input argument
     */

    @Override
    default void accept(T obj) {
        this.send(obj);
    }

}
