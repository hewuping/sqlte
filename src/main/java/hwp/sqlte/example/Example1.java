package hwp.sqlte.example;

import hwp.sqlte.ArgsProvider;
import hwp.sqlte.Sql;

import java.util.Optional;

/**
 * @author Zero
 * Created on 2017/3/27.
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

    public String getUserOrders2(String username) throws Exception {
        return Sql.runOnTx(conn -> {
            conn.update(builder -> {
                builder.sql("update orders set user_id=1");
                builder.where(where -> {
                    where.add("username = ?", "zeri");
                });
            });
            return "haha";
        });
    }

    public String getUserOrders3(String username) throws Exception {
        return Sql.runOnTx(conn -> {
            conn.batchUpdate(builder -> {
                builder.sql("update orders set user_id=1");
            }, new ArgsProvider() {
                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public Object[] nextArgs() {
                    return new Object[0];
                }
            });
            return "haha";
        });
    }

}
