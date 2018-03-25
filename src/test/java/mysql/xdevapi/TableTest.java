package mysql.xdevapi;

import com.mysql.cj.api.xdevapi.Schema;
import com.mysql.cj.api.xdevapi.Table;
import com.mysql.cj.api.xdevapi.XSession;
import com.mysql.cj.core.admin.ServerController;
import com.mysql.cj.xdevapi.XSessionFactory;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Zero
 *         Created by Zero on 2017/6/18 0018.
 */
public class TableTest {

    @Test
    public void test1() throws IOException {
//        ServerController serverController = new ServerController("D:\\Program Files\\MariaDB 10.1");
//        Process process = serverController.start();
//        System.out.println(process.isAlive());
        XSession mySession = new XSessionFactory().getSession("mysqlx://localhost:33060/test?user=root&password=");
        Schema db = mySession.getSchema("test");
        Table table = db.getTable("auth_users");
        table.insert("username", "passwrod", "password_salt")
                .values("May", "1234", "zz").execute();
    }

}
