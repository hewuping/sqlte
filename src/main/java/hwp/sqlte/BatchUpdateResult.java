package hwp.sqlte;

import java.sql.Statement;

/**
 * @author Zero
 * Created on 2018/5/16.
 */
public class BatchUpdateResult {

    /**
     * <p>MySQL: 当rewriteBatchedStatements=true时, 该值是不可靠的, 优点是可以大幅提升插入效率</p>
     *
     * <p>当rewriteBatchedStatements=true时, JDBC可以通过以下方法获取要获取成功插入的行</p>
     *
     * <pre>ResultSet keys = statement.getGeneratedKeys();//MySQL只有自增ID才会返回
     *    if (keys != null) {
     *      if (keys.last()) {
     *      count.add(keys.getRow());
     *  }
     * }</pre>
     *
     * <p>see: https://bugs.mysql.com/bug.php?id=68562,
     * https://bugs.mysql.com/bug.php?id=61213</p>
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

    public void addBatchResult(int[] rs) {
        for (int r : rs) {
            if (r > 0) {
                affectedRows += r;
            } else if (r == Statement.SUCCESS_NO_INFO) {
                successNoInfoCount++;
            } else if (r == Statement.EXECUTE_FAILED) {
                failedCount++;
            }
        }
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
