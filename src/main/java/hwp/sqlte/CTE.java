package hwp.sqlte;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class CTE {


    private final Map<String, SqlBuilder> map = new LinkedHashMap<>();

    public CTE() {
    }


    public CTE set(String name, Consumer<SqlBuilder> consumer) {
        SqlBuilder sub = new SqlBuilder();
        consumer.accept(sub);
        map.put(name, sub);
        return this;
    }

    protected void forEach(BiConsumer<String, SqlBuilder> consumer) {
        map.forEach(consumer);
    }

}
