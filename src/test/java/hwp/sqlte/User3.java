package hwp.sqlte;

import java.time.LocalDateTime;

@Table(name = "users")
public class User3 {

    @Id
    public Integer id;
    public String username;
    public String email;
    public String password;
    //test enum
    public PasswordSalt passwordSalt = PasswordSalt.A123456;
    public LocalDateTime updatedTime;


    enum PasswordSalt {
        A123456, B123456, C123456
    }

    public User3() {
    }


    public User3(String username, String email, String password) {
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
        sb.append(", password_salt='").append(passwordSalt).append('\'');
//        sb.append(", creation_time=").append(creation_time);
        sb.append(", updated_time=").append(updatedTime);
        sb.append('}');
        return sb.toString();
    }


}
