package hwp.sqlte;

import java.util.Date;

@Table(name = "users")
public class User {

    @Id(generate = true)
    public Integer id;
//    @Column(name = "USERNAME")
    public String username;
    public String email;
    public String password;
    public Gender gender;
    // LocalDateTime //MySQL driver 5.x is not support
    public Date updatedTime;


    public User() {
    }


    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public static User of(String username) {
        return new User(username, username + "@example.com", "123456");
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("User{");
        sb.append("id=").append(id);
        sb.append(", username='").append(username).append('\'');
        sb.append(", email='").append(email).append('\'');
        sb.append(", password='").append(password).append('\'');
        sb.append(", gender='").append(gender).append('\'');
//        sb.append(", creation_time=").append(creation_time);
        sb.append(", updated_time=").append(updatedTime);
        sb.append('}');
        return sb.toString();
    }


}
