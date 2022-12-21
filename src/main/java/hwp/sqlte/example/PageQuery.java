package hwp.sqlte.example;

import hwp.sqlte.Pageable;
import hwp.sqlte.util.Sort;

public class PageQuery implements Pageable {

    private Sort sort;
    private int page = 0;
    private int pageSize = 10;

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
