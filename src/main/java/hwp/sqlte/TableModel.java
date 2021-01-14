/*
 * Copyright (c) 2021. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 *       Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 *       Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 *       Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 *       Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package hwp.sqlte;

import java.util.List;

/**
 * @author Zero
 * Created on 2021/1/12.
 */
public class TableModel<T> {
    private final List<T> data;
    private final long rowCount;
    private Integer pageSize;
    private Integer page;

    public TableModel(List<T> data, long rowCount) {
        this.data = data;
        this.rowCount = rowCount;
    }

    public TableModel(List<T> data, long rowCount, Integer pageSize, Integer page) {
        this.data = data;
        this.rowCount = rowCount;
        this.pageSize = pageSize;
        this.page = page;
    }

    public List<T> getData() {
        return data;
    }

    public long getRowCount() {
        return rowCount;
    }

}
