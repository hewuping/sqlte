package hwp.sqlte;

/**
 * @author Zero
 * Created on 2018/5/16.
 */
public class BatchUpdateResult {

    /**
     * <p>MySQL: 当rewriteBatchedStatements=true时, 该值是不可靠的<br/>
     *
     * <p>当rewriteBatchedStatements=true时, JDBC可以通过以下方法获取要获取成功插入的行<br/>
     * <p><pre>ResultSet keys = statement.getGeneratedKeys();//MySQL只有自增ID才会返回
     *    if (keys != null) {
     *      if (keys.last()) {
     *      count.add(keys.getRow());
     *  }
     * }</pre></p>
     *
     * <p>see: https://bugs.mysql.com/bug.php?id=68562
     */
    public long affectedRows;
    public long successNoInfoCount;
    public long failedCount;

    public static final BatchUpdateResult EMPTY = new BatchUpdateResult();


    public boolean hasSuccessNoInfo() {
        return successNoInfoCount > 0;
    }

    public boolean hasFailed() {
        return failedCount > 0;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("affectedRows=").append(affectedRows);
        sb.append(", successNoInfoCount=").append(successNoInfoCount);
        sb.append(", failedCount=").append(failedCount);
        sb.append('}');
        return sb.toString();
    }
}
