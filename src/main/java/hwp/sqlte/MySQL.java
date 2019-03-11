/*
 * Copyright (c) 2019. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 *       Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 *       Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 *       Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 *       Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package hwp.sqlte;

import java.util.function.Function;

/**
 * @author Zero
 * Created on 2019/2/20.
 */
public class MySQL {

    public static final Function<String, String> TO_INSERT_IGNORE_INTO =
            sql -> sql.replace("INSERT INTO", "INSERT IGNORE INTO");

}
