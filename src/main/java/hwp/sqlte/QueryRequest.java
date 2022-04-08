package hwp.sqlte;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * <pre>
 * 请求:
 *
 *  {
 *      "query": {
 *          "name": "",
 *          "age": 12
 *      },
 *    "sort": {
 *          "name": "DESC",
 *          "age": "ASC"
 *  },
 *  "from": 5,
 *  "size": 20
 * }
 *
 * 处理:
 *
 * db.query(sql -> {
 *   sql.orderBy(order -> {
 *      request.onSort("name", direction -> order.by("name", direction));
 *   });
 * });
 * </pre>
 *
 * @author Zero
 * Created on 2021/2/19.
 */
public class QueryRequest {
    private final static Gson gson = new Gson();

    private JsonObject query;
    private LinkedHashMap<String, Direction> sort;
    private int from = 0;
    private int size = 10;


    /**
     * 当存在指定的排序名称时, 执行后续操作
     *
     * @param name
     * @param consumer
     */
    public void onSort(String name, Consumer<Direction> consumer) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(consumer);
        if (sort == null) {
            return;
        }
        Direction direction = sort.get(name);
        if (direction != null) {
            consumer.accept(direction);
        }
    }


    public <T> T getQueryAs(Class<T> clazz) {
        Objects.requireNonNull(query, "query is null");
        Objects.requireNonNull(clazz);
        return gson.fromJson(query, clazz);
    }


    public JsonObject getQuery() {
        return query;
    }

    public void setQuery(JsonObject query) {
        this.query = query;
    }

    public LinkedHashMap<String, Direction> getSort() {
        return sort;
    }

    public void setSort(LinkedHashMap<String, Direction> sort) {
        this.sort = sort;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public static QueryRequest fromJson(String json) {
        return gson.fromJson(json, QueryRequest.class);
    }

    //eg: {"query":{"name":"","age":12},"sort":{"name":"DESC","age":"ASC"},"from":5,"size":20}
}
