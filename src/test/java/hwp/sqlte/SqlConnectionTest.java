package hwp.sqlte;

//import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

//import com.mysql.cj.jdbc.MysqlDataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import hwp.sqlte.example.User;
import org.junit.*;
import org.postgresql.ds.PGSimpleDataSource;

import java.net.URL;
import java.sql.*;
import java.util.*;

/**
 * @author Zero
 * Created by Zero on 2017/6/17 0017.
 */
public class SqlConnectionTest {

    private SqlConnection conn;

    @BeforeClass
    public static void beforeClass() throws Exception {
        PGSimpleDataSource pgds = new PGSimpleDataSource();
        pgds.setURL("jdbc:postgresql://10.1.1.203:5432/testdb");
        pgds.setUser("zero");
        pgds.setPassword("123456");
//        Sql.config().setDataSource(mysqlDS);
//          Sql.config().setDataSource(pgds);
        Sql.config().setDataSource(hikariDataSource());
        init();
    }

    @Before
    public void before() {
        conn = Sql.newConnection();
        conn.setAutoCommit(false);
        conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
    }

    @After
    public void after() {
        if (conn != null) {
            conn.close();
        }
    }

    private static HikariDataSource hikariDataSource() {
        HikariConfig config = new HikariConfig();
        config.setAutoCommit(true);
        config.setJdbcUrl("jdbc:mysql://localhost:3306/test?serverTimezone=UTC&characterEncoding=utf-8&rewriteBatchedStatements=true&useAffectedRows=true");
        config.setUsername("root");
        config.setMaximumPoolSize(2);
        config.setConnectionInitSql("select 1");
        config.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
//        config.addDataSourceProperty("rewriteBatchedStatements",true);
//        config.addDataSourceProperty("useAffectedRows","true");
        return new HikariDataSource(config);
    }

    private static void init() {
        Sql.use(conn -> {
            URL resource = SqlConnectionTest.class.getResource("/init_mysql.sql");
            ScriptRunner runner = new ScriptRunner(true, true);
            runner.runScript(conn.connection(), resource);
        });
    }

    @Test
    public void testInsertBean() {
        User user = new User("Frank", "frank.fu@xx.com", "123456");
        conn.insertBean(user, "users");
        User _user = conn.query("select * from users where username =?", "Frank").first(User::new);
        Assert.assertNotNull(_user);
        Assert.assertEquals(_user.username, user.username);
        Assert.assertEquals(_user.email, user.email);
        Assert.assertEquals(_user.password, user.password);
    }

    @Test
    public void query() throws Exception {
        User user = conn.query("select * from users where username =?", "Frank").first(User::new);
        Assert.assertNull(user);
    }

    @Test
    public void query1() throws Exception {
        Optional<User> user = conn.query("select * from user where username=?", "Zero").first(User.MAPPER);
        user.ifPresent(user1 -> conn.query("select * from orders where user_id=?", user1.id));
    }

    @Test
    public void query2() throws Exception {
        insertUser();
        Row row = conn.query("select * from users where username=?", "May").first();
        Assert.assertNotNull(row.getValue("username"));
        Assert.assertNotNull(row.getValue("email"));
        Assert.assertNull(row.getValue("xxx"));
        Optional<String> xxx = row.getOptValue("xxx");
        Assert.assertFalse(xxx.isPresent());
    }


    @Test
    public void query3() throws Exception {
        insertUser();
        String sql = "select * from users where username=?";

        //select list
        List<User> users1 = conn.query(sql, "May").map(User.MAPPER);
        Assert.assertTrue(users1.size() > 0);
        List<User> users2 = conn.query(sql, "May").map(User::new);
        Assert.assertTrue(users2.size() > 0);

        //select one
        Optional<User> frank = conn.query(sql, "Frank").first(User.MAPPER);
        frank.ifPresent(user -> Assert.assertEquals(user.password, "123456"));
    }

    @Test
    public void query4() throws Exception {
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

    @Test
    public void queryResultSet() throws Exception {
        conn.query("select * from user where username=?", rs -> {
            try {
                String name = rs.getString("username");
                Assert.assertEquals("Frank", name);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, "Frank");
    }

    @Test
    public void testSqlBuilder() throws Exception {
        insertUser();
        String username = "May";
        String email = null;
        String password = "123456";
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
        SqlResultSet rows = conn.query("select * from users");
        User rs = user.orElse(null);
        Assert.assertNotNull(rs);
    }

    @Test
    public void insert() throws Exception {
        conn.insert("users", "username,password,password_salt", "may", "123456", "xxx");
    }

    private void insertUser() {
        User user = new User("May", "may@xxx.com", "123456");
        user.password_salt = "***";
        conn.insertBean(user, "users");
    }

    @Test
    public void insertBean() throws Exception {
        conn.setAutoCommit(false);
        insertUser();
    }

    @Test
    public void insertMap1() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("username", "Zero");
        map.put("password", "123456");
        map.put("email", "zero@ccjk.com");
        conn.insertMap("users", map);
    }

    @Test
    public void insertMap2() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("username", "Zero");
        map.put("password", "123456");
        map.put("email", "zero@ccjk.com");
        conn.insertMap("users", map, "id");
        //{id=6, password=123456, email=zero@ccjk.com, username=Zero}
        System.out.println(map);
        Assert.assertNotNull(map.get("id"));
    }

    @Test
    public void batchInsert1() throws SQLException {
        conn.batchUpdate("INSERT INTO users (`email`, `username`)  VALUES (?, ?)", executor -> {
            executor.exec("bb@example.com", "bb");
            executor.exec("aa@example.com", "aa");
        });
    }

    @Test
    public void batchInsert2() throws SQLException {
        conn.batchInsert("users", "email, username", executor -> {
            executor.exec("bb@example.com", "bb");
            executor.exec("aa@example.com", "aa");
        });
    }

    @Test
    public void batchInsert3() throws SQLException {
        List<User> users = new ArrayList<>();
        int size = 20000;
        for (int i = 0; i < size; i++) {
            users.add(new User("zero" + i, "zero@ccjk.com", "123456"));
        }
        BatchUpdateResult result = conn.batchUpdate("INSERT IGNORE INTO users (`email`, `username`)  VALUES (?, ?)", 1000, users, (executor, user) -> {
            executor.exec(user.email, user.username);
        });
        if (result.hasSuccessNoInfo()) {
            Assert.assertEquals(result.successNoInfoCount, size);
        } else {
            Assert.assertEquals(result.affectedRows, size);
        }
    }

    @Test
    public void batchInsert4() throws Exception {
        List<User> users = new ArrayList<>();
        int size = 20000;
        for (int i = 0; i < size; i++) {
            users.add(new User("zero" + i, "zero@ccjk.com", "123456"));
        }
        UnsafeCount count = new UnsafeCount();
        PreparedStatement ps = conn.prepareStatement("INSERT IGNORE INTO users (`email`, `username`)  VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
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

    @Test
    public void batchUpdate_insert5() throws SQLException {
        List<User> users = new ArrayList<>();
        int size = 20;
        for (int i = 0; i < size; i++) {
            users.add(new User("zero" + i, "zero@ccjk.com", "123456"));
        }
        BatchUpdateResult result = conn.batchUpdate("INSERT INTO users (`email`, `username`)  VALUES (?, ?)", executor -> {
            users.forEach(user -> executor.exec(user.email, user.username));
        });
        if (result.hasSuccessNoInfo()) {
            Assert.assertTrue(result.successNoInfoCount > 0);
        } else {
            Assert.assertEquals(size, result.affectedRows);
        }
    }

    @Test
    public void batchUpdate_insert6() throws SQLException {
        List<User> users = new ArrayList<>();
        int size = 20;
        for (int i = 0; i < size; i++) {
            users.add(new User("zero" + i, "zero@ccjk.com", "123456"));
        }
        BatchUpdateResult result = conn.batchInsert("users", "email, username", executor -> {
            users.forEach(user -> executor.exec(user.email, user.username));
        });
        if (result.hasSuccessNoInfo()) {
            Assert.assertTrue(result.successNoInfoCount > 0);
        } else {
            Assert.assertEquals(size, result.affectedRows);
        }
    }

    //    @Test
    public void batchUpdate_insert_pgsql() throws SQLException {
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


    @Test
    public void update() throws Exception {
        conn.update(new User("Frank", "123456", "test@gmail.com"), "users", where -> {
            where.and("username=?", "Frank");
        });
        SqlResultSet rows = conn.query("select * from users where username =? limit 10", "Frank");
        List<User> users = rows.map(User::new);
        System.out.println(users.size());
    }

    @Test
    public void update2() throws Exception {
        Row data = new Row().set("id", "123456").set("username", "Zero").set("email", "bb@example.com");
        conn.insertMap("users", data, "id");
        int update = conn.update(data.set("username", "zero1"), "users", where -> {
            where.and("id=?", data.get("id"));
        });
        Assert.assertEquals(update, 1);
        //OR
        update = conn.updateByPks(data.set("username", "zero2"), "users", "id");
        Assert.assertEquals(update, 1);
        //OR
        update = conn.updateByPks(data.set("username", "zero3"), "users");// pk default: id
        Assert.assertEquals(update, 1);
    }

    //    @Test
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