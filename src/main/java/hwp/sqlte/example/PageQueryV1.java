package hwp.sqlte.example;

import com.google.gson.*;
import hwp.sqlte.ClassInfo;
import hwp.sqlte.Direction;
import hwp.sqlte.util.Sort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

@Deprecated
public class PageQueryV1<T> {

    private static final Logger logger = LoggerFactory.getLogger(PageQueryV1.class);

    private T query;

    private Sort sort;

    private int page = 0;
    private int pageSize = 10;


    public static <T> PageQueryV1<T> fromJson(String json, Class<T> queryClass) {
        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
        PageQueryV1<T> request = new PageQueryV1<T>();
        Gson gson = new Gson();
        JsonElement query = obj.get("query");
        if (query != null) {
            request.query = gson.fromJson(query, queryClass);
        }
        JsonElement sort = obj.get("sort");
        if (sort != null) {
            request.sort = gson.fromJson(sort, Sort.class);
        }
        JsonPrimitive page = obj.getAsJsonPrimitive("page");
        if (page != null) {
            request.page = Math.max(1, page.getAsInt());
        }
        JsonPrimitive pageSize = obj.getAsJsonPrimitive("pageSize");
        if (pageSize != null) {
            request.pageSize = Math.max(1, pageSize.getAsInt());
        }
        return request;
    }

    public void clean() {
        if (sort == null || sort.isEmpty()) {
            return;
        }
        ClassInfo classInfo = ClassInfo.getClassInfo(query.getClass());
        Set<Map.Entry<String, Direction>> entries = sort.entrySet();
        Iterator<Map.Entry<String, Direction>> it = entries.iterator();
        while (it.hasNext()) {
            Map.Entry<String, Direction> entry = it.next();
            String column = classInfo.getColumn(entry.getKey());
            if (column == null) {
                logger.warn("忽略排序字段: {}", entry.getKey());
                it.remove();
            }
        }
    }

    public void accept(Consumer<T> consumer) {
        consumer.accept(query);
    }

    public T getQuery() {
        return query;
    }

    public void setQuery(T query) {
        this.query = query;
    }

    public Sort getSort() {
        return sort;
    }

    public void setSort(Sort sort) {
        this.sort = sort;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

}
