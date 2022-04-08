package hwp.sqlte;

/**
 * @author Zero
 * Created on 2021/2/18.
 */
public class SafeDelete implements Sql {
    private final String table;
    private Where where;

    private SafeDelete(String table) {
        this.table = table;
    }

    public static SafeDelete from(String table) {
        return new SafeDelete(table);
    }

    public SafeDelete where(Where where) {
        this.where = where;
        return this;
    }

    @Override
    public String sql() {
        boolean unsafe = where == null || where.isEmpty();
        StringBuilder builder = new StringBuilder();
        builder.append("DELETE FROM ").append(table);
        if (unsafe) {
            builder.append("WHERE ").append(where);
        } else {
            throw new SqlteException("Deny dangerous delete operations: " + builder);
        }
        return builder.toString();
    }

    @Override
    public Object[] args() {
        boolean unconditional = where == null || where.isEmpty();
        return unconditional ? new Object[0] : where.args().toArray();
    }

}
