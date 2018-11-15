package hwp.sqlte;

/**
 * @author Zero
 * Created on 2017/3/22.
 */
public class Order {


    private StringBuilder orderSql = new StringBuilder();

    public Order() {
    }

    public Order ifBy(boolean _if, String column, String desc) {
        if (_if) {
            this.by(column, desc);
        }
        return this;
    }

    public Order by(String column) {
        return this.asc(column);
    }

    public Order asc(String column) {
        return this.by(column, "ASC");
    }

    public Order desc(String column) {
        return this.by(column, "DESC");
    }

    public Order by(String column, String order) {
        if (orderSql.length() == 0) {
            orderSql.append(" ORDER BY");
        }
        orderSql.append(orderSql.length() == 9 ? " " : ", ");
        if ("DESC".equalsIgnoreCase(order)) {
            orderSql.append(column).append(" ").append(order);
        } else {
            orderSql.append(column).append(" ").append("ASC");
        }
        return this;
    }

    public String sql() {
        return orderSql.toString();
    }

    @Override
    public String toString() {
        return sql();
    }

}
