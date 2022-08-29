package hwp.sqlte;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
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
    private static Gson gson = new Gson();

    private JsonObject query;
    private LinkedHashMap<String, Direction> sort;
    private int from = 0;
    private int size = 10;

    public static void setGson(Gson _gson) {
        gson = _gson;
    }


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

    /**
     * 从JSON中构建对象， 例如： {"query":{"name":"","age":12},"sort":{"name":"DESC","age":"ASC"},"from":5,"size":20}
     *
     * @param json
     * @return
     */
    public static QueryRequest fromJson(String json) {
        return gson.fromJson(json, QueryRequest.class);
    }

    //name=Frank&age=18~20&sort=name:desc,age:asc&from=5&size=20
    public static QueryRequest fromQueryString(String queryString) {
        QueryRequest request = new QueryRequest();
        Map<String, LinkedList<String>> map = queryStringToMap(queryString, StandardCharsets.UTF_8);
        LinkedList<String> list = map.remove("sort");
        if (list != null && list.peek() != null) {
            String sortValue = list.peek();
            request.sort = new LinkedHashMap<>();
            String[] pairs = sortValue.split(",");
            for (String pair : pairs) {
                String[] kv = pair.split(":", 2);
                request.sort.put(kv[0], Direction.find(kv[1]));
            }
        }
        list = map.remove("from");
        if (list != null && list.peek() != null) {
            request.from = Integer.valueOf(list.peek());
        }
        list = map.remove("size");
        if (list != null && list.peek() != null) {
            request.size = Integer.valueOf(list.peek());
        }
        JsonObject query = new JsonObject();
        map.forEach((key, values) -> {
            if (values.isEmpty()) {
                query.addProperty(key, "");
            } else if (values.size() == 1) {
                String value = values.peek();
                String[] kv = value.split("~", 2);
                if (kv.length == 2) {
                    JsonObject v = new JsonObject();
                    if (StringUtils.isNumber(kv[0]) && StringUtils.isNumber(kv[1])) {
                        v.addProperty("start", Long.valueOf(kv[0]));
                        v.addProperty("end", Long.valueOf(kv[1]));
                    } else {
                        v.addProperty("start", kv[0]);
                        v.addProperty("end", kv[1]);
                    }
                    query.add(key, v);
                } else {
                    query.addProperty(key, value);
                }
            } else {
                JsonArray array = new JsonArray();
                for (String value : values) {
                    array.add(value);
                }
                query.add(key, array);
            }
        });
        request.query = query;
        return request;
    }

    private static Map<String, LinkedList<String>> queryStringToMap(String query, Charset charset) {
        Map<String, LinkedList<String>> queryPairs = new LinkedHashMap<>();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            try {
                String[] kv = pair.split("=", 2);
                String key = URLDecoder.decode(kv[0], charset.name());//兼容Java8
                LinkedList<String> values = queryPairs.get(key);
                if (values == null) {
                    values = new LinkedList<>();
                    queryPairs.put(key, values);
                }
                String value = kv.length == 1 ? null : URLDecoder.decode(kv[1], charset.name());//兼容Java8
                values.add(value);
            } catch (UnsupportedEncodingException e) {
                //ignore
            }
        }
        return queryPairs;
    }

}
