package hwp.sqlte;

public interface ArgsProvider {

    boolean hasNext();

    Object[] nextArgs();

    default int batchSize() {
        return 1000;
    }

}
