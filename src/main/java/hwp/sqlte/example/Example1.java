package hwp.sqlte.example;

import hwp.sqlte.Session;
import hwp.sqlte.Sql;
import hwp.sqlte.SqlConnection;
import hwp.sqlte.SqlFunction;

import java.util.Optional;

/**
 * @author Zero
 *         Created on 2017/3/27.
 */
public class Example1 {

    public Object getUserOrders(String username) {
        return Session.runOnTx(session -> {
            Optional<User> user = session.query("select * from user where uername=?", username).first(User.MAPPER);
            if (user.isPresent()) {
                System.out.println(user.get());
                session.use("db2").query("select * from orders wehre user_id=?", user.get().username);
            }
            return user.get();
        });
    }

    public static void main(String[] args) throws Exception {
        Session.runOnTx(session -> {
            session.insert("insert into user(username,password) values(?,?)", "").handleResult(rs->{
                rs.firstRow().get("id");
            });
//            session.insert("table",obj)
            return null;
        });
        Sql.runOnTx(conn -> {
//            conn.query()
            return "";
        });

    }
}
