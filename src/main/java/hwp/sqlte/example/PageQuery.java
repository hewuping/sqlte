package hwp.sqlte.example;

import hwp.sqlte.Direction;
import hwp.sqlte.Pageable;
import hwp.sqlte.Sort;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 该类作为复杂查询(分页, 排序, 搜索, 过滤)的基类
 */
public class PageQuery implements Pageable {

    private Sort sort = new Sort();
    private int page = 0;
    private int pageSize = 10;

    public Sort getSort() {
        return sort;
    }

    public Direction getSort(String name) {
        return sort.get(name);
    }

    public Direction getSort(String name, Direction def) {
        if (sort == null) {
            return def;
        }
        return sort.getOrDefault(name, def);
    }

    public void setSort(Sort sort) {
        this.sort = sort;
    }

    /**
     * Spring Web 中如果通过URL传递参数并绑定实体, 参数名会比较奇怪, 比如: sort["name"]=DESC。
     * <p>
     * 这里提供更优雅的方案, sort=name:desc;age:asc;xxx;
     */
    public void setSort(String sortStr) {
        if (sortStr == null || sortStr.isEmpty()) {
            return;
        }
        for (String entry : sortStr.split(";")) {
            String[] item = entry.split(":", 2);
            if (item.length == 1) {
                sort.put(item[0], Direction.ASC);
                continue;
            }
            if (item.length == 2) {
                sort.put(item[0], Direction.find(item[1], Direction.ASC));
                continue;
            }
        }
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

    public void sort(Consumer<Sort> consumer) {
        consumer.accept(sort);
    }

    @Deprecated
    public void acceptSort(Consumer<Sort> consumer) {
        consumer.accept(sort);
    }

    @Deprecated
    public void acceptPage(BiConsumer<Integer, Integer> consumer) {
        consumer.accept(page, pageSize);
    }


}
