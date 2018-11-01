package hwp.sqlte;

/**
 * @author Zero
 *         Created on 2018/5/16.
 */
public class BatchUpdateResult {

    /**
     * <p>MySQL: 当rewriteBatchedStatements=true时, 该值是不可靠的<br/>
     *
     * <p>see: https://bugs.mysql.com/bug.php?id=68562
     */
    public long affectedRows;
    public long successNoInfoCount;
    public long failedCount;


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
