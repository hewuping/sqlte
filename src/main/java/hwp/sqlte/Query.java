package hwp.sqlte;

import hwp.sqlte.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author Zero
 * Created on 2020/12/21.
 */
public class Query implements Sql {//QueryDsl

    private String select;
    private String from;
    private Where where;
    private String groupBy;
    private Having having;
    private String orderBy;
    private Integer offset;
    private Integer limit;

    private Class<?> selectClass;


    public Query select(String columns) {
        this.select = columns;
        return this;
    }

    public Query select(String columns, boolean distinct) {
        this.select = distinct ? "DISTINCT " + columns : columns;
        return this;
    }

    public Query from(String from) {
        this.from = from;
        return this;
    }

    public <T> Query select(Class<T> clazz) {
        ClassInfo classInfo = ClassInfo.getClassInfo(clazz);
        this.select = "*";
        this.from = classInfo.getTableName();
        this.selectClass = clazz;
        return this;
    }

    public Query where(String sql, Object... args) {
        this.where = new Where();
        this.where.append(sql, args);
        return this;
    }

    public Query where(Where where) {
        this.where = where;
        return this;
    }

    public Query where(Consumer<Where> consumer) {
        this.where = new Where();
        consumer.accept(this.where);
        return this;
    }

    public Query groupBy(String groupBy) {
        Objects.requireNonNull(groupBy);
        this.groupBy = groupBy;
        return this;
    }

    public Query having(Having having) {
        this.having = having;
        return this;
    }

    public Query having(Consumer<Having> consumer) {
        this.having = new Having();
        consumer.accept(this.having);
        return this;
    }

    public Query orderBy(String orderBy) {
        Objects.requireNonNull(orderBy);
        this.orderBy = orderBy;
        return this;
    }

    public Query orderBy(Consumer<Order> consumer) {
        Order order = new Order();
        consumer.accept(order);
        return orderBy(order);
    }

    public Query orderBy(Order order) {
        if (order != null && !order.isEmpty()) {
            this.orderBy = order.sql();
        }
        return this;
    }

    public Query limit(int offset, int limit) {
        this.offset = offset;
        this.limit = limit;
        return this;
    }

    public Query paging(int page, int pageSize) {
        this.offset = Math.max(0, page - 1) * pageSize;
        this.limit = Math.max(pageSize, 1);
        return this;
    }

    public Query paging(Pageable pageable) {
        return paging(pageable.getPage(), pageable.getPageSize());
    }

    public String toSql(char separator) {
        Objects.requireNonNull(select);
        Objects.requireNonNull(from);
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ").append(select).append(separator);
        sql.append("FROM ").append(from).append(separator);
        if (where != null && !where.isEmpty()) {
            sql.append("WHERE ").append(where).append(separator);
        }
        if (StringUtils.isNotEmpty(groupBy)) {
            sql.append("GROUP BY ").append(groupBy).append(separator);
            if (having != null && !having.isEmpty()) {
                sql.append("HAVING ").append(having).append(separator);
            }
        }
        if (StringUtils.isNotEmpty(orderBy)) {
            sql.append("ORDER BY ").append(orderBy).append(separator);
        }
        if (offset != null || limit != null) {
            sql.append("LIMIT ").append(offset == null ? 0 : offset);
            if (limit != null) {
                sql.append(',').append(limit);
            }
        }
        return sql.toString();
    }

    private static String format(Object date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        return format.format(date);
    }

    @Override
    public String sql() {
        return toSql('\n');
    }

    @Override
    public Object[] args() {
        List<Object> args = new ArrayList<>(4);
        if (where != null && !where.isEmpty()) {
            args.addAll(where.args());
        }
        if (StringUtils.isNotEmpty(groupBy) && (having != null && !having.isEmpty())) {
            args.addAll(having.args());
        }
        return args.toArray();
    }

    @Override
    public String toString() {
        String sql = toSql(' ');
        Object[] args = args();
        if (args == null || args.length == 0) {
            return sql;
        }
        int argIndex = args.length - 1;
        StringBuilder newSql = new StringBuilder(sql);
        for (int i = newSql.length() - 1; i >= 0; i--) {
            char c = newSql.charAt(i);
            if (c == '?') {
                Object arg = args[argIndex--];
                String replace = "?";
                if (arg instanceof String) {
                    replace = (String) arg;
                } else if (arg instanceof Date) {
                    replace = "'" + format(arg) + "'";
                } else if (arg instanceof Number) {
                    replace = arg.toString();
                } else {
                    replace = "'" + arg.toString() + "'";
                }
                newSql.replace(i, i + 1, replace);
            }
        }
        return newSql.toString();
    }

    public static void main(String[] args) {
        Query sql = new Query();
        sql.select("*").from("user").where(where -> {
            where.and("creation_time > ?", new Date());
            where.and("uid > ?", 10);
        }).groupBy("uid").orderBy("name desc");

//        sql.query("user");
        System.out.println(sql);
    }
}
