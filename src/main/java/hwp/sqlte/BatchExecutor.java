package hwp.sqlte;

/**
 * @author Zero
 *         Created on 2018/3/27.
 */
public interface BatchExecutor {

    void exec(Object... args);

}