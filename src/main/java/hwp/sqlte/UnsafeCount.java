package hwp.sqlte;

/**
 * @author Zero
 *         Created on 2018/4/4.
 */
class UnsafeCount {
    int count;

    public int add(int v) {
        this.count += v;
        return this.count;
    }

    public int getAndIncrement() {
        return count++;
    }

    public int incrementAndGet() {
        return ++count;
    }

    public int get() {
        return this.count;
    }

    public void set(int value) {
        this.count = value;
    }


}
