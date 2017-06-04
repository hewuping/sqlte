package hwp.sqlte;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Zero
 *         Created on 2017/3/20.
 */
public class ResultSet implements Iterable<Row> {

    private List<String> culumns;
    private List<Row> rows;

    public static final ResultSet EMPTY = new ResultSet(
            Collections.unmodifiableList(Collections.emptyList()),
            Collections.unmodifiableList(Collections.emptyList()));

    public ResultSet(List<String> culumns, List<Row> rows) {
        this.culumns = culumns;
        this.rows = rows;
    }

    public List<String> getCulumns() {
        return culumns;
    }

    public List<Row> getRows() {
        return rows;
    }

    public Row firstRow() {
        if (this.rows != null && this.rows.size() > 0) {
            return this.rows.get(0);
        }
        return null;
    }

    protected void unmodifiableRows() {
        this.rows = Collections.unmodifiableList(this.rows);
    }

    @Override
    public Iterator<Row> iterator() {
        return this.rows.iterator();
    }

    @Override
    public void forEach(Consumer<? super Row> action) {
        this.rows.forEach(action);
    }

    @Override
    public Spliterator<Row> spliterator() {
        return this.rows.spliterator();
    }

    public Stream<Row> stream() {
        return StreamSupport.stream(spliterator(), false);
    }
}
