package hwp.sqlte.example;

import hwp.sqlte.Sql;

import java.util.Optional;

/**
 * @author Zero
 *         Created on 2017/3/27.
 */
public class Example1 {

    public Object getUserOrders(String username) throws Exception {
        return Sql.runOnTx(conn -> {
            Optional<User> user = conn.query("select * from user where uername=?", username).first(User.MAPPER);
            if (user.isPresent()) {
                System.out.println(user.get());
                conn.query("select * from orders wehre user_id=?", user.get().username);
            }
            return user.orElse(null);
        });
    }


}
