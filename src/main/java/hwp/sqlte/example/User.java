package hwp.sqlte.example;

import hwp.sqlte.Id;
import hwp.sqlte.Row;
import hwp.sqlte.RowMapper;

/**
 * @author Zero
 *         Created on 2017/3/27.
 */
public class User {

    public String id="1";
    public String username;
    public String email;
    public String password;
    public String password_salt="sfwerx";

    public User() {
    }


    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public static final RowMapper<User> MAPPER = new RowMapper<User>() {
        @Override
        public User map(Row row) {
            User user = new User();
            user.username = row.getString("username");
            user.password = row.getString("password");
            return user;
        }
    };

}
