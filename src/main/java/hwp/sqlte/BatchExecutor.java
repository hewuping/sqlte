package hwp.sqlte;

/**
 * @author Zero
 *         Created on 2018/3/27.
 */
public interface BatchExecutor {

    //Producer
    void exec(Object... args);

}
