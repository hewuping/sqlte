package hwp.sqlte.gg;

/**
 * @author Zero
 *         Created on 2017/3/21.
 */
public class NameParameter {

    String name;
    int index;//from 1
    boolean nullable;

    public NameParameter(String name, int index) {
        this.name = name;
        this.index = index;
    }

}
