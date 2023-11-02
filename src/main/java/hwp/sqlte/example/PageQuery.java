package hwp.sqlte.example;

import hwp.sqlte.Direction;
import hwp.sqlte.Pageable;
import hwp.sqlte.util.Sort;

import java.util.Collections;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class PageQuery implements Pageable {

    private Sort sort;
    private int page = 0;
    private int pageSize = 10;

    public Sort getSort() {
        if (sort == null) {
            sort = new Sort();
        }
        return sort;
    }

    public void setSort(Sort sort) {
        this.sort = sort;
    }


//    public Direction sort(String column) {
//        Objects.requireNonNull(column, "column cannot be null");
//        if (sort == null) {
//            return null;
//        }
//        return this.sort.match(column, null);
//    }


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

    public void acceptSort(Consumer<Sort> consumer) {
        if (sort != null) {
            consumer.accept(sort);
        }
    }

    public void acceptPage(BiConsumer<Integer, Integer> consumer) {
        consumer.accept(page, pageSize);
    }


}
