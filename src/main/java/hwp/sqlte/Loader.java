package hwp.sqlte;

import java.util.function.Consumer;

public interface Loader<T> extends Consumer<Sender<T>> {

}
