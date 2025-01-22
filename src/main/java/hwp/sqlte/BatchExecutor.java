package hwp.sqlte;

/**
 * @author Zero
 * Created on 2018/3/27.
 */
public interface BatchExecutor {

    //Producer
    void exec(Object... args);

    //  void exec(BatchUpdateOptions options, Object... args);

    default int batchSize() {
        return 1000;
    }

}
