package hwp.sqlte;

//import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

//import com.mysql.cj.jdbc.MysqlDataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import hwp.sqlte.example.Example1;
import hwp.sqlte.example.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.postgresql.ds.PGSimpleDataSource;

import java.sql.*;
import java.util.*;

/**
 * @author Zero
 *         Created by Zero on 2017/6/17 0017.
 */
public class SqlConnectionTest {

    @Before
    public void setUp() throws Exception {
//       DataSource ds= JdbcConnectionPool.create("jdbc:h2:~/test2", "sa", "");

//        MysqlDataSource ds = new MysqlDataSource();
//        com.mysql.cj.jdbc.MysqlConnectionPoolDataSource mysqlDS = new com.mysql.cj.jdbc.MysqlConnectionPoolDataSource();
//        com.mysql.jdbc.jdbc2.optional.MysqlDataSource ds = new com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource();
//        mysqlDS.setURL("jdbc:mysql://localhost:3306/test?serverTimezone=UTC&characterEncoding=utf-8");
//        mysqlDS.setURL("jdbc:mysql://localhost:3306/test?serverTimezone=UTC&characterEncoding=utf-8&rewriteBatchedStatements=true");
//        mysqlDS.setUser("root");
//        mysqlDS.getConnection().close();

        PGSimpleDataSource pgds = new PGSimpleDataSource();
        pgds.setURL("jdbc:postgresql://10.1.1.203:5432/testdb");
        pgds.setUser("zero");
        pgds.setPassword("123456");
//        Sql.config().setDataSource(mysqlDS);
//          Sql.config().setDataSource(pgds);
        Sql.config().setDataSource(hikariDataSource());

    }

    private HikariDataSource hikariDataSource() {
        HikariConfig config = new HikariConfig();
        config.setAutoCommit(true);
        config.setJdbcUrl("jdbc:mysql://localhost:3306/test?serverTimezone=UTC&characterEncoding=utf-8&rewriteBatchedStatements=true&useAffectedRows=true");
        config.setUsername("root");
//        config.addDataSourceProperty("rewriteBatchedStatements",true);
//        config.addDataSourceProperty("useAffectedRows","true");
        return new HikariDataSource(config);
    }

    @Test
    public void testDd() throws SQLException {
        Sql.newConnection().close();
    }

    @Test
    public void query() throws Exception {
        try (SqlConnection conn = Sql.newConnection()) {
            User user = conn.query("select * from user where username =?", "Frank").first(User::new);
        }
    }

    @Test
    public void query1() throws Exception {
        Example1 example = new Example1();
        example.queryExample1("Frank");
    }

    @Test
    public void query2() throws Exception {
        try (SqlConnection conn = Sql.newConnection()) {
            Row row = conn.query("select * from users where username=?", "Frank").first();
            String name = row.getValue("username");
            int age = row.getValue("age");
            Optional<String> xxx = row.getOptValue("xxx");
            Assert.assertEquals(name, "Frank");
            Assert.assertEquals(age, 18);
            Assert.assertFalse(xxx.isPresent());
        }
    }


    @Test
    public void query3() throws Exception {
        List<User> franks = Sql.newConnection().query("select * from users where username=?", "Frank").map(User.MAPPER);
        System.out.println(franks);
        Assert.assertTrue(franks.size() > 0);
        Optional<User> frank = Sql.newConnection().query("select * from users where username=?", "Frank").first(User.MAPPER);
        Assert.assertEquals(frank.get().password, "123456");
    }

    @Test
    public void query4() throws Exception {
        try (SqlConnection conn = Sql.newConnection()) {
            String username = "Frank";
            //simple
            conn.query("select * from users where username=?", username).forEach(row -> {
                Assert.assertEquals("frank@ccjk.com", row.get("email"));
                Assert.assertNull(row.get("unk"));
            });

            //use mapper
            conn.query("select * from users where username=?", username).map(User.MAPPER).forEach(user -> {
                Assert.assertEquals("frank@ccjk.com", user.email);
            });

            //RowHandler (big data RowHandler)
            conn.query(new SimpleSql("select * from users where username=?", username), row -> {
                Assert.assertEquals("frank@ccjk.com", row.getString("email"));
                return true;
            });
        }
    }

    @Test
    public void queryResultSet() throws Exception {
        try (SqlConnection conn = Sql.newConnection()) {
            conn.query("select * from user where username=?", rs -> {
                try {
                    Assert.assertTrue(ResultSet.class.isInstance(rs));
                    String name = rs.getString("username");
                    Assert.assertEquals("Frank", name);
                } catch (SQLException e) {

                }
            }, "Frank");
        }
    }

    @Test
    public void testSqlBuilder() throws Exception {
        String username = "Frank";
        String email = null;
        String password = "123456";
        SqlConnection conn = Sql.newConnection();
        Optional<User> user = conn.query(sql -> {
            sql.sql("select * from users");
            sql.where(where -> {
                where.and(username != null, "username =?", username);//if username has value, use
                where.and("password =?", password);
//                where.and("email =?", email);//IS NULL, NOT NULL
//                where.or("age =?", 18);
            });
            System.out.println(sql);
        }).first(User.MAPPER);
        User rs = user.orElse(null);
        Assert.assertNotNull(rs);
    }

    @Test
    public void insert() throws Exception {
        try (SqlConnection conn = Sql.newConnection()) {
//            conn.insert("insert into auth_user(username,password,password_salt) values(?,?,?)", "zero", "123456", "xxx");
            conn.insert(Helper.makeInsertSql("auth_user2", "username,password,password_salt"), "may", "123456", "xxx");
        }
    }

    @Test
    public void insertBean() throws Exception {
        try (SqlConnection conn = Sql.newConnection()) {
            User user = new User("May", "123456", "xswenli");
//        conn.insertBean(user, "users", true);
            System.out.println(user);
//        SqlResultSet rows = conn.query("select * from users where username =? limit 10", "May");
//        List<User> users = rows.map(row -> row.convert(new User()));
//        System.out.println(users);
        }
    }

    @Test
    public void insertMap() throws Exception {
        try (SqlConnection conn = Sql.newConnection()) {
            Map<String, Object> map = new HashMap<>();
            map.put("username", "Zero");
            map.put("password", "123456");
            map.put("email", "zero@ccjk.com");
//        conn.insertMap("users", new HashMap<>(map));
//            conn.insertMap("users", map, "id");
            conn.insertMap("users", map);
            //{GENERATED_KEY=6, password=123456, email=zero@ccjk.com, username=Zero}
            System.out.println(map);
        }
    }

    @Test
    public void batchInsert() throws SQLException {
        try (SqlConnection conn = Sql.newConnection()) {
            conn.batchUpdate("INSERT INTO users (`email`, `username`)  VALUES (?, ?)", executor -> {
                executor.exec("bb@example.com", "bb");
                executor.exec("aa@example.com", "aa");
            });
        }
    }

    @Test
    public void batchUpdate_insert() throws SQLException {
        try (SqlConnection conn = Sql.newConnection()) {
            List<User> users = new ArrayList<>();
            int size = 20000;
            for (int i = 0; i < size; i++) {
                users.add(new User("zero" + i, "zero@ccjk.com", "123456"));
            }
            BatchUpdateResult result = conn.batchUpdate("INSERT IGNORE INTO users2 (`email`, `username`)  VALUES (?, ?)", 1000, users, (executor, user) -> {
                executor.exec(user.email, user.username);
            });
            if (result.hasSuccessNoInfo()) {
                Assert.assertEquals(result.successNoInfoCount, size);
            } else {
                Assert.assertEquals(result.affectedRows, size);
            }
        }
    }

    @Test
    public void batchUpdate_insert4() throws Exception {
        try (SqlConnection conn = Sql.newConnection()) {
            List<User> users = new ArrayList<>();
            int size = 20000;
            for (int i = 0; i < size; i++) {
                users.add(new User("zero" + i, "zero@ccjk.com", "123456"));
            }
            UnsafeCount count = new UnsafeCount();
            PreparedStatement ps = conn.prepareStatement("INSERT IGNORE INTO users2 (`email`, `username`)  VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
            conn.batchUpdate(ps, 1000, executor -> {
                users.forEach(user -> executor.exec(user.email, user.username));
            }, (statement, rs) -> {
                try {
                    ResultSet keys = statement.getGeneratedKeys();//MySQL只有自增ID才会返回
                    if (keys != null) {
                        if (keys.last()) {
                            count.add(keys.getRow());//TODO
                        }
                    }
                } catch (SQLException e) {
                    throw new UncheckedException(e);
                }
            });
            System.out.println(count);
        }
    }

    @Test
    public void batchUpdate_insert2() throws SQLException {
        try (SqlConnection conn = Sql.newConnection()) {
            List<User> users = new ArrayList<>();
            int size = 20000;
            for (int i = 0; i < size; i++) {
                users.add(new User("zero" + i, "zero@ccjk.com", "123456"));
            }
            BatchUpdateResult result = conn.batchUpdate("INSERT IGNORE  INTO users2 (`email`, `username`)  VALUES (?, ?)", 1000, users, (executor, user) -> {
                executor.exec(user.email, user.username);
            });
            if (result.hasSuccessNoInfo()) {
                Assert.assertEquals(result.successNoInfoCount, size);
            } else {
                Assert.assertEquals(result.affectedRows, size);
            }
        }
    }

    @Test
    public void batchUpdate_insert_pgsql() throws SQLException {
        try (SqlConnection conn = Sql.newConnection()) {
            List<User> users = new ArrayList<>();
            int size = 20000;
            for (int i = 0; i < size; i++) {
                users.add(new User("zero" + i, "zero@ccjk.com", "123456"));
            }
            BatchUpdateResult result = conn.batchUpdate("INSERT INTO users (email, username)  VALUES (?, ?) ON CONFLICT (username) DO NOTHING", 1000, users, (executor, user) -> {
                executor.exec(user.email, user.username);
            });
            if (result.hasSuccessNoInfo()) {
                Assert.assertEquals(result.successNoInfoCount, size);
            } else {
                Assert.assertEquals(result.affectedRows, size);
            }
        }
    }


    @Test
    public void batchUpdate_insert3() throws SQLException {
        try (SqlConnection conn = Sql.newConnection()) {
            List<User> users = new ArrayList<>();
            int size = 20;
            for (int i = 0; i < size; i++) {
                users.add(new User("zero" + i, "zero@ccjk.com", "123456"));
            }
            BatchUpdateResult result = conn.batchUpdate("INSERT INTO users (`email`, `username`)  VALUES (?, ?)", executor -> {
                users.forEach(user -> executor.exec(user.email, user.username));
            });
            Assert.assertEquals(result.affectedRows, size);
        }
    }

    @Test
    public void update() throws Exception {
        try (SqlConnection conn = Sql.newConnection()) {
            conn.update(new User("Frank", "123456", "test@gmail.com"), "users", where -> {
                where.and("username=?", "Frank");
            });
            SqlResultSet rows = conn.query("select * from users where username =? limit 10", "Frank");
            List<User> users = rows.map(User::new);
            System.out.println(users.size());
        }
    }

    @Test
    public void update2() throws Exception {
        try (SqlConnection conn = Sql.newConnection()) {
            //
            Row data = new Row().set("id", "123").set("username", "Zero").set("email", "bb@example.com");
            conn.update(data, "users", where -> {
                where.and("id=?", data.get("id"));
            });
            //OR
            conn.updateById(data, "users", "id");

            //OR
            conn.updateById(data, "users");
        }
    }

    @Test
    public void ss() throws SQLException {
        try (SqlConnection conn = Sql.newConnection()) {
            conn.setAutoCommit(false);
            conn.setReadOnly(false);
            PreparedStatement statement = conn.prepareStatement("insert ignore into users2 (username,email) value(?,?)");
            for (int i = 0; i < 2; i++) {
                statement.setString(1, "zero" + i);
                statement.setString(2, "zero@ccjk.com");
                statement.addBatch();
            }

            int[] i = statement.executeBatch();
//            statement.clearBatch();
            System.out.println(Arrays.toString(i));
            statement.addBatch("DELETE FROM usersw2 WHERE email='zero@ccjk.com'");
            statement.addBatch("DELETE FROM users2 WHERE email='zero@ccjk.com'");
            i = statement.executeBatch();
            System.out.println(Arrays.toString(i));
            conn.commit();
        } catch (BatchUpdateException e) {
            System.out.println(Arrays.toString(e.getUpdateCounts()));
        }
    }

}