package hwp.sqlte.example;

import hwp.sqlte.Row;
import hwp.sqlte.Sql;
import hwp.sqlte.SqlConnection;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Zero
 *         Created on 2017/3/27.
 */
public class Example1 {

    public User queryExample(String username) throws Exception {
        SqlConnection conn = Sql.newConnection();
        User user = conn.query("select * from user where username =?", username).first(User::new);
        return user;
    }

    public User queryExample1(String username) throws Exception {
        SqlConnection conn = Sql.newConnection();
        Optional<User> user = conn.query("select * from user where username=?", username).first(User.MAPPER);
        if (user.isPresent()) {
            System.out.println(user.get());
            conn.query("select * from orders where user_id=?", user.get().id);
        }
        return user.orElse(null);
    }

    public Map<String, Object> queryExample2(String username) throws Exception {
        Row row = Sql.newConnection().query("select * from user where username=?", username).firstRow();
        return row;
    }

    public List<User> queryExample3(String username) throws Exception {
        return Sql.newConnection().query("select * from user where username=?", username).flatMap(User.MAPPER);
    }

    public void queryExample4(String username) throws Exception {
        Sql.newConnection().query("select * from user where username=?", username).forEach(row -> {
            System.out.println(row.get("email"));
        });

        Sql.newConnection().query("select * from user where username=?", username).flatMap(User.MAPPER).forEach(user -> {
            System.out.println(user.email);
        });
    }

    public void queryExample5(String username) throws Exception {
        Sql.newConnection().query("select * from user where username=?", rs -> {
            try {
                String name = rs.getString("username");
                System.out.println(name);
                return true;
            } catch (Exception e) {
                return false;
            }
        }, username);
    }

    public User queryExample3(String username, String email, String password) throws Exception {
        SqlConnection conn = Sql.newConnection();
        Optional<User> user = conn.query(sql -> {
            sql.sql("select * from user");
            sql.where(where -> {
                where.add(username != null, "username =?", username);//if username!=null
                where.add(email != null, "email =?", email);
                where.add("password =?", password);
            });
        }).first(User.MAPPER);
        return user.orElse(null);
    }

    //insert
    public void insertExample(String username, String email, String password) throws Exception {
        SqlConnection conn = Sql.newConnection();
        conn.insert("insert into user(username, email, password) value(?, ?, ?)", username, email, password);
    }

    public void insertExample2(String username, String email, String password) throws Exception {
        SqlConnection conn = Sql.newConnection();
        Optional<Long> idOpt = conn.incInsert("insert into user(username, email, password) value(?, ?, ?)", username, email, password);
        System.out.println("user_id: " + idOpt.get());
    }

    public void insertExample3(String username, String email, String password) throws Exception {
        SqlConnection conn = Sql.newConnection();
        User user = new User("May", "may@gmail.com", "123456");
        conn.insert(user);//table name: user
        conn.insert(user, "user");
    }


    public void updateExample() throws Exception {
        SqlConnection conn = Sql.newConnection();
        conn.update("update user set username=? where user_id=?", "Cindy", 123);
        //OR
        conn.update(builder -> {
            builder.sql("update user set username=?").args("Cindy");
            builder.where(where -> {
                where.add("user_id=?", 123);
            });
        });
    }

    public void batchUpdateExample(List<User> users) throws Exception {
        SqlConnection conn = Sql.newConnection();
        conn.batchUpdate("update user set username=?, password=? where id=?", users, (args, user) -> {
            args.add(user.username);
            args.add(user.password);
            args.add(user.id);
        });
        //OR
        conn.batchUpdate2("update user set username=?, password=? where id=?", users, (args, user) -> {
            args.setArgs(user.username, user.password);
        });
    }

}
