package hwp.sqlte.example;

import hwp.sqlte.Row;
import hwp.sqlte.RowMapper;

import java.util.Date;

/**
 * @author Zero
 *         Created on 2017/3/27.
 */
public class User {

    public Integer id;
    public String username;
    public String email;
    public String password;
    public String password_salt="sfwerx";
//    public Date creation_time;
    public Date updated_time;

    public User() {
    }


    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("User{");
        sb.append("id=").append(id);
        sb.append(", username='").append(username).append('\'');
        sb.append(", email='").append(email).append('\'');
        sb.append(", password='").append(password).append('\'');
        sb.append(", password_salt='").append(password_salt).append('\'');
//        sb.append(", creation_time=").append(creation_time);
        sb.append(", updated_time=").append(updated_time);
        sb.append('}');
        return sb.toString();
    }

    public static final RowMapper<User> MAPPER = new RowMapper<User>() {
        @Override
        public User map(Row row) {
            User user = new User();
            user.username = row.getString("username");
            user.password = row.getString("password");
            user.email = row.getString("email");
            return user;
        }
    };

}
