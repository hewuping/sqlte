package hwp.sqlte.example;

import hwp.sqlte.Row;
import hwp.sqlte.RowMapper;

/**
 * @author Zero
 *         Created on 2017/3/27.
 */
public class User {

    public String username;
    public String password;

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
