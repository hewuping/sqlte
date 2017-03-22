package hwp.sqlte;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Zero
 *         Created on 2017/3/22.
 */
public class Order {

//    private String name;
//    private String solr;
//
//    public static Order desc(String culumnName) {
//
//    }

    private List<Order> orders = new ArrayList<>(2);

    public Order then(Order order) {
        return this;
    }

}
