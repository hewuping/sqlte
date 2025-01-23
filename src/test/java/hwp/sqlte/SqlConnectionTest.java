package hwp.sqlte;

//import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

//import com.mysql.cj.jdbc.MysqlDataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.*;

import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Zero
 * Created by Zero on 2017/6/17 0017.
 */
public class SqlConnectionTest {

    private SqlConnection conn;

    private static final String dbname = "h2";//h2, mysql, pgsql

    @BeforeClass
    public static void beforeClass() {
        HikariConfig config = new HikariConfig();
        config.setAutoCommit(true);
        config.setMaximumPoolSize(2);//包括空闲链接和正在使用的连接, 也是程序可使用的最大连接数
        config.setConnectionTimeout(5_000);// 当达到最大连接数时, getConnection()会阻塞, 该值是阻塞超时值
        config.setConnectionInitSql("select 1");
        config.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
//        config.addDataSourceProperty("rewriteBatchedStatements",true);
//        config.addDataSourceProperty("useAffectedRows","true");

        //h2
        config.setJdbcUrl("jdbc:h2:mem:h2-memory");
        //mysql
        if ("mysql".equals(dbname)) {
            config.setJdbcUrl("jdbc:mysql://localhost:3306/test?serverTimezone=UTC&characterEncoding=utf-8&useAffectedRows=true");
            config.setUsername("root");
        }
        //pgsql
        if ("pgsql".equals(dbname)) {
            config.setJdbcUrl("jdbc:postgresql://10.1.1.203:5432/testdb");
            config.setUsername("zero");
            config.setPassword("123456");
        }

        Sql.config().setDataSource(new HikariDataSource(config));

        Sql.use(conn -> {
            URL resource = SqlConnectionTest.class.getResource("/init_" + dbname + ".sql");
            ScriptRunner runner = new ScriptRunner(true, true);
            runner.runScript(conn.connection(), resource);
        });
    }


    @Before
    public void before() {
        conn = Sql.open();
        conn.setAutoCommit(false);
        conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
    }

    @After
    public void after() {
        deleteAllUsers();
        if (conn != null) {
            conn.close();
        }
    }

    private boolean isMySQL() {
        return "mysql".equals(dbname);
    }

    @Test
    public void showMetaData() throws SQLException {
        Connection connection = conn.connection();
        DatabaseMetaData metaData = connection.getMetaData();
        String product = metaData.getDatabaseProductName();
        String escape = metaData.getSearchStringEscape();
        String keywords = metaData.getSQLKeywords();
        String driverName = metaData.getDriverName();
//        String extraNameCharacters = metaData.getExtraNameCharacters();
        System.out.println("-------------------------------");
        System.out.println("product: " + product);
        System.out.println("escape: " + escape);
        System.out.println("keywords: " + keywords);
        System.out.println("driverName: " + driverName);
        System.out.println("-------------------------------");
    }

    ////////////////////////////////////ORM////////////////////////////////////////////////////////////////


    private User insertUser() {
        User user = User.of("May");
        user.gender = Gender.FEMALE;
        conn.insert(user, "users");
        return user;
    }

    private List<User> insertUsers(int size) {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            int id = 1 + i;
            User user = User.of("zero" + id);
            user.id = id;
            user.updatedTime = new Date();
            users.add(user);
        }
        conn.batchInsert(users);
        return users;
    }

    private void deleteAllUsers() {
        conn.executeUpdate("delete from users");
    }

    @Test
    public void testExecuteUpdate() {
        List<User> users = insertUsers(3);
        Object[] ids = users.stream().map(user -> user.id).toArray();
        Assert.assertEquals(1, conn.executeUpdate("delete from users where id = " + ids[0]));
        Assert.assertEquals(1, conn.executeUpdate("delete from users where id = ?", ids[1]));
        Assert.assertEquals(1, conn.executeUpdate(sql -> {
            sql.delete("users").where(where -> {
                where.and("id=?", ids[2]);
            });
        }));
    }

    @Test
    public void testExecuteUpdate2() {
        insertUsers(3);
        Assert.assertEquals(3, conn.executeUpdate("update  users set username='java'"));
    }

    @Test
    public void testInsertBean() {
        conn.setAutoCommit(false);
        insertUser();
    }

    @Test
    public void testInsertUser() {
        User user = new User("May", "may@example.com", "123456");
        conn.insert(user, "users");
        Assert.assertNotNull(user.id);
    }

    @Test
    public void testLoad() { // Single primary key
        User user = new User("May", "may@example.com", "123456");
        user.id = 123456;
        conn.insert(user, "users");
        conn.load(User.class, 123456);
    }

    @Test
    public void testReload() { // Single primary key OR Composite primary key
        User user = new User("May", "may@example.com", "123456");
        conn.insert(user, "users");

        User tmp = new User();
        tmp.id = user.id;
        conn.reload(tmp);
        Assert.assertNotNull(tmp.password);
    }

    @Test
    public void testUpdateBean() {
        User user = new User("May", "may@example.com", "123456");
        conn.insert(user, "users");
        String newPassword = ThreadLocalRandom.current().nextInt() + "@";
        user.password = newPassword;
        conn.update(user, "password");
        User User = conn.query("select * from users where password=?", newPassword).first(User.class);
        Assert.assertNotNull(User);
    }

    @Test
    public void testDeleteBean() {
        User user = new User("May", "may@example.com", "123456");
        conn.insert(user, "users");
        Assert.assertTrue(conn.delete(user, "users"));
    }

    @Test
    public void testDeleteAll1() {
        List<User> users = insertUsers(20);
        List<User> list1 = conn.listAll(User.class);
        Assert.assertEquals(users.size(), list1.size());
        int affectedRows = conn.deleteAll(users);
        Assert.assertEquals(users.size(), affectedRows);
        List<User> list2 = conn.listAll(User.class);
        Assert.assertEquals(0, list2.size());
    }

    @Test
    public void testDeleteAll2() {
        insertUsers(10);
        Assert.assertEquals(10, conn.listAll(User.class).size());
        conn.delete(User.class, Where.EMPTY);
        Assert.assertTrue(conn.listAll(User.class).isEmpty());
    }

    @Test
    public void testDeleteByExampe1() {
        User user = new User("May", "may@example.com", "123456");
        conn.insert(user);
        conn.deleteByExample(user);
        Assert.assertTrue(conn.listAll(User.class).isEmpty());
    }

    @Test
    public void testDeleteByExampe2() {
        User user = new User("May", "may@example.com", "123456");
        conn.insert(user);
        conn.deleteByExample(User::new, example -> {
            example.username = "May";
        });
        Assert.assertTrue(conn.listAll(User.class).isEmpty());
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void testQuery() {
        User user = conn.query("select * from users u where  u.username =?", "Frank").first(User::new);
        Assert.assertNull(user);
    }

    @Test
    public void testQuery1() {
        insertUser();
        User user = conn.query("select * from users where username=?", "May").first(User.class);
        Assert.assertNotNull(user);
    }

    @Test
    public void testQuery2() {
        insertUser();
        Row row = conn.query("select * from users where username=?", "May").first();
        Assert.assertNotNull(row.getValue("username"));
        Assert.assertNotNull(row.getValue("email"));
        Assert.assertNull(row.getValue("xxx"));
        Optional<String> xxx = row.getOptValue("xxx");
        Assert.assertFalse(xxx.isPresent());
    }


    @Test
    public void testQuery3() {
        insertUser();
        String sql = "select * from users where username=?";
        // 如果User中有单个参数的构造器, 会导致冲突
        //select list
        List<User> users1 = conn.query(sql, "May").list(User.class);
        Assert.assertFalse(users1.isEmpty());
        List<User> users2 = conn.query(sql, "May").list(User::new);
        Assert.assertFalse(users2.isEmpty());

        //select one
        User frank = conn.query(sql, "Frank").first(User.class);
        Assert.assertNull(frank);
    }

    @Test
    public void testQuery4() {
        String username = "Frank";
        //simple
        conn.query("select * from users where username=?", username).forEach(row -> {
            Assert.assertEquals("frank@example.com", row.get("email"));
            Assert.assertNull(row.get("unk"));
        });

        //use mapper
        conn.query("select * from users where username=?", username).list(User.class).forEach(user -> {
            Assert.assertEquals("frank@example.com", user.email);
        });

        //RowHandler (big data RowHandler)
        conn.query(Sql.create("select * from users where username=?", username), row -> {
            Assert.assertEquals("frank@example.com", row.getString("email"));
            return true;
        });
    }

    @Test
    public void testQueryPage() {
        for (int i = 0; i < 100; i++) {
            insertUser();
        }
        Page<User> page = conn.queryPage(User.class, sql -> sql.select(User.class).paging(2, 10));
        Assert.assertEquals(10, page.getData().size());
    }

    @Test
    public void testListClassIds() {
//        conn.query(sql -> sql.select(""));
        User user1 = insertUser();
        User User = insertUser();
        List<User> list = conn.list(User.class, Arrays.asList(user1.id, User.id));
        Assert.assertEquals(2, list.size());
    }

    @Test
    public void testQueryResultSet() {
        conn.query(Sql.create("select * from users where username=?", "Frank"), rs -> {
            try {
                String name = rs.getString("username");
                Assert.assertEquals("Frank", name);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void testQueryFirst_Enum() {
        insertUser();
        Gender gender = conn.query("select gender from users where username =?", "May").first(Gender.class);
        Assert.assertEquals(Gender.FEMALE, gender);
    }

    @Test
    public void testQuery_EnumList() {
        insertUser();
        List<Gender> list = conn.query("select gender from users where username =?", "May").list(Gender.class);
        Assert.assertEquals(1, list.size());
    }

    @Test
    public void testFirstExample() {
        User user = new User("Zero", "zero@example.com", "123456");
        user.id = 1;
        conn.insert(user);
        User t1 = conn.firstExample(user);
        Assert.assertNotNull(t1);
        User t2 = conn.firstExample(new User("Zero", null, null));
        Assert.assertNotNull(t2);
        User t3 = conn.firstExample(new User(null, "zero@example.com", null));
        Assert.assertNotNull(t3);
        User t4 = conn.firstExample(new User(null, null, "123456"));
        Assert.assertNotNull(t4);
    }

    @Test
    public void testListExample() {
        conn.insert(new User("Zero1", "zero@example.com", "123456"));
        conn.insert(new User("Zero2", "zero@example.com", "123456"));
        User example = new User();
        example.email = "zero@example.com";
        List<User> users = conn.listExample(example);
        Assert.assertEquals(2, users.size());
    }

    @Test
    public void testListExample2() {
        conn.insert(new User("Zero1", "zero@example.com", "123456"));
        conn.insert(new User("Zero2", "zero@example.com", "123456"));
        UserQuery query = new UserQuery();
        List<User> users = conn.listExample(User.class, query);
        Assert.assertEquals(2, users.size());
        UserQuery query2 = new UserQuery();
        query2.email = "zero@example.com";
        users = conn.listExample(User.class, query2);
        Assert.assertEquals(2, users.size());
        try {
            UserQuery query3 = new UserQuery();
            // 测试查询不存在的字段
            query3.name = "zero1";
            users = conn.listExample(User.class, query3);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof SqlteException);//eg: Column "NAME1" not found;
        }
    }


    @Test
    public void testContains() {
        User user = new User("Zero", "zero@example.com", "123456");
        conn.insert(user);
        Assert.assertTrue(conn.contains(user));
        user.email = "";
        Assert.assertTrue(conn.contains(user));
        user.email = "frank@example.com";
        Assert.assertFalse(conn.contains(user));
    }

    @Test
    public void testList() {
        insertUser();
        insertUser();
        List<User> users = conn.list(User.class, where -> {
            where.and("username =?", "May");
        });
        Assert.assertEquals(2, users.size());
    }

    @Test
    public void testListAll() {
        insertUser();
        insertUser();
        List<User> users = conn.listAll(User.class);
        Assert.assertEquals(2, users.size());
    }

    @Test
    public void testListByIds() {
        User user1 = insertUser();
        User User = insertUser();
        List<Integer> ids = Arrays.asList(user1.id, User.id);
        List<User> users = conn.list(User.class, ids);
        Assert.assertEquals(2, users.size());
    }

    @Test
    public void testListThen() {
        insertUser();
        List<User> users = conn.query("select * from users").list(User::new, (user, row) -> {
            System.out.println(user);
            System.out.println(row);
        });
        Assert.assertEquals(1, users.size());
    }

    @Test
    public void testFirstThen() {
        insertUser();
        User result = conn.query("select * from users").first(User::new, (user, row) -> {
            System.out.println(user);
            System.out.println(row);
        });
        Assert.assertNotNull(result);
    }

    @Test
    public void testSqlBuilder() {
        insertUser();
        String username = "May";
        String email = null;
        String password = "123456";
        User user = conn.query(sql -> {
            sql.sql("select * from users");
            sql.where(where -> {
                where.and(username != null, "username =?", username);//if username has value, use
                where.and("password =?", password);
//                where.and("email =?", email);//IS NULL, NOT NULL
//                where.or("age =?", 18);
            });
            System.out.println(sql);
        }).first(User.class);
        Assert.assertNotNull(user);
    }

    @Test
    public void testInsert() {
        conn.insert("users", "username,password,gender", "may", "123456", "MALE");
        conn.insert("users", "username,password,gender", "may", "123456", "male");
    }


    @Test
    public void testInsertMap1() {
        Map<String, Object> map = new HashMap<>();
        map.put("username", "Zero");
        map.put("password", "123456");
        map.put("email", "zero@example.com");
        conn.insertMap("users", map);
    }

    @Test
    public void testInsertMap2() {
        Map<String, Object> map = new HashMap<>();
        map.put("username", "Zero");
        map.put("password", "123456");
        map.put("email", "zero@example.com");
        conn.insertMap("users", map, "id");
        //{id=6, password=123456, email=zero@example.com, username=Zero}
        System.out.println(map);
        Assert.assertNotNull(map.get("id"));
    }

    @Test
    public void testInsertMap3() {
        conn.insertMap("users", map -> {
            map.put("username", "Zero");
            map.put("password", "123456");
            map.put("email", "zero@example.com");
        });
    }

    @Test
    public void testBatchInsert1() {
        conn.batchUpdate("INSERT INTO users (email, username)  VALUES (?, ?)", executor -> {
            executor.exec("bb@example.com", "bb");
            executor.exec("aa@example.com", "aa");
        });
    }

    @Test
    public void testBatchInsert2() {
        conn.batchInsert("users", "email, username", executor -> {
            executor.exec("bb@example.com", "bb");
            executor.exec("aa@example.com", "aa");
        });
    }

    @Test
    public void testBatchInsert3() {
        List<User> users = new ArrayList<>();
        int size = 20000;
        for (int i = 0; i < size; i++) {
            users.add(new User("zero" + i, "zero@example.com", "123456"));
        }
        BatchUpdateResult result = conn.batchUpdate("INSERT /*IGNORE*/ INTO users (email, username)  VALUES (?, ?)", executor -> {
            for (User user : users) {
                executor.exec(user.email, user.username);
            }
        });
        if (result.hasSuccessNoInfo()) {
            Assert.assertEquals(result.successNoInfoCount, size);
        } else {
            Assert.assertEquals(result.affectedRows, size);
        }
    }

//    @Test
//    public void testBatchInsert4() throws SQLException {
//        List<User> users = new ArrayList<>();
//        int size = 200;
//        for (int i = 0; i < size; i++) {
//            users.add(new User("zero" + i, "zero@example.com", "123456"));
//        }
//        Counter count = new Counter();
//        PreparedStatement ps = conn.prepareStatement("INSERT /*IGNORE*/ INTO users (email, username)  VALUES (?, ?)",
//                Statement.RETURN_GENERATED_KEYS);
////        PreparedStatement ps = conn.prepareStatement("INSERT /*IGNORE*/ INTO users (email, username)  VALUES (?, ?)",
////                ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY ,  ResultSet.CLOSE_CURSORS_AT_COMMIT);
//        BatchUpdateResult result = conn.batchUpdate(ps, 10, executor -> {
//            users.forEach(user -> executor.exec(user.email, user.username));
//        }, (keys) -> {
//            try {
//                if (keys != null) {
//                    //MySQL只有自增ID才会返回
//                    //bug: h2: Feature not supported
////                     ResultSet.TYPE_SCROLL_xxx
////                    if (keys.last()) {
////                        count.add(keys.getRow());
////                    }
//                    while (keys.next()) {//Statement.RETURN_GENERATED_KEYS 才会返回, 但是很耗性能
//                        int cc = keys.getMetaData().getColumnCount();
//                        //MySQL: GENERATED_KEY
//                        //H2: 1.4.193 返回固定名称:identity  1.4.197 返回实际列名
//                        //PGSQL: 返回实际列名, 如果没有指定返回列名而是Statement.RETURN_GENERATED_KEYS, 则会返回所有列数据
//                        String name = keys.getMetaData().getColumnName(1);
//                        count.add(1);
//                    }
//                }
//            } catch (SQLException e) {
//                throw new SqlteException(e);
//            }
//        });
//        ps.close();
//    }

    @Test
    public void testBatchInsertBeans() {
        //pgsql9.x 自增的id列可以为null, pgsql10.x是不行的
        List<User> users = new ArrayList<>();
        int size = 20000;
        for (int i = 0; i < size; i++) {
            User user = new User("zero" + i, "zero@example.com", "123456");
            user.id = i;
            user.updatedTime = new Date();
            users.add(user);
        }
        conn.batchInsert(users);
        Set<Integer> ids = new HashSet<>(size);
        for (User user : users) {
            ids.add(user.id);
        }
        Assert.assertEquals(size, ids.size());
    }

    @Test
    public void testBatchInsertBeans2() {
        int size = 2000;
        BatchUpdateResult result = conn.batchInsert(User.class, exe -> {
            for (int i = 0; i < size; i++) {
                User user = new User("zero" + i, "zero@example.com", "123456");
                user.id = i;
                user.updatedTime = new Date();
                exe.accept(user);
            }
        }, UpdateOptions.DEFAULT);
        if (result.hasSuccessNoInfo()) {
            Assert.assertTrue(result.successNoInfoCount > 0);
        } else {
            Assert.assertEquals(size, result.affectedRows);
        }
    }

    @Test
    public void testBatchInsertBeans3() {
        //pgsql9.x 自增的id列可以为null, pgsql10.x是不行的
        List<User> users1 = new ArrayList<>();
        int size = 1000;
        for (int i = 0; i < size; i++) {
            User user = new User("zero" + i, "zero@example.com", "123456");
            user.id = 1000 + i;
            user.updatedTime = new Date();
            users1.add(user);
        }
        BatchUpdateResult result1 = conn.batchInsert(users1);
        Assert.assertEquals(result1.affectedRows, users1.size());

        List<User> users2 = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            User user = new User("frank" + i, "frank@example.com", "123456");
            user.id = 2000 + i;
            user.updatedTime = new Date();
            users2.add(user);
        }

        BatchUpdateResult result2 = conn.batchInsert(users2);
        Assert.assertEquals(result1.affectedRows, users1.size());
    }


    @Test
    public void testBatchUpdate_insert5() {
        List<User> users = new ArrayList<>();
        int size = 20;
        for (int i = 0; i < size; i++) {
            users.add(new User("zero" + i, "zero@example.com", "123456"));
        }
        BatchUpdateResult result = conn.batchUpdate("INSERT INTO users (email, username)  VALUES (?, ?)", executor -> {
            users.forEach(user -> executor.exec(user.email, user.username));
        });
        if (result.hasSuccessNoInfo()) {
            Assert.assertTrue(result.successNoInfoCount > 0);
        } else {
            Assert.assertEquals(size, result.affectedRows);
        }
        deleteAllUsers();
    }

    @Test
    public void testBatchUpdate_insert6() {
        List<User> users = new ArrayList<>();
        int size = 20;
        for (int i = 0; i < size; i++) {
            users.add(new User("zero" + i, "zero@example.com", "123456"));
        }
        BatchUpdateResult result = conn.batchInsert("users", "email, username", executor -> {
            users.forEach(user -> executor.exec(user.email, user.username));
        });
        if (result.hasSuccessNoInfo()) {
            Assert.assertTrue(result.successNoInfoCount > 0);
        } else {
            Assert.assertEquals(size, result.affectedRows);
        }
        deleteAllUsers();
    }

    @Test
    public void testBatchUpdate_insert_pgsql() {
        if (dbname.equals("pgsql")) {
            List<User> users = new ArrayList<>();
            int size = 200;
            for (int i = 0; i < size; i++) {
                users.add(new User("zero" + i, "zero@example.com", "123456"));
            }
            String sql = "INSERT INTO users (email, username)  VALUES (?, ?) ON CONFLICT (username) DO NOTHING";
            BatchUpdateResult result = conn.batchUpdate(sql, executor -> {
                for (User user : users) {
                    executor.exec(user.email, user.username);
                }
            });
            if (result.hasSuccessNoInfo()) {
                Assert.assertEquals(result.successNoInfoCount, size);
            } else {
                Assert.assertEquals(result.affectedRows, size);
            }
        }
    }


    @Test
    public void testUpdate2() {
        Row data = new Row().set("username", "Zero").set("email", "bb@example.com");
        conn.insertMap("users", data, "id");
        int update = conn.update("users", data.set("username", "zero1"), where -> {
            where.and("id=?", data.get("id"));
        });
        Assert.assertEquals(1, update);
        update = conn.update("users", row -> {
            row.set("username", "zero00").set("email", "zero@example.com");
        }, where -> {
            where.and("id=?", data.get("id"));
        });
        Assert.assertEquals(1, update);
    }

    @Test
    public void testUpdate3() {
        User user = new User("May", "may@example.com", "123456");
        conn.insert(user, "users");
        user.username = "My new name";
        user.email = null;
        conn.update(user, options -> options.setUpdateColumns("username").ignoreNullValues());
        User _user = conn.tryGet(User::new, user.id);
        Assert.assertEquals(_user.username, user.username);
        Assert.assertNotNull(_user.email);
    }

    @Test
    public void testUpdateMap() {
        User user1 = insertUser();
        conn.update("users", row -> {
            row.put("email", "foo@example.com");
        }, where -> {
            where.and("id=?", user1.id);
        });
        User User = conn.tryGet(User::new, user1.id);
        Assert.assertNotEquals(user1.email, User.email);
    }


    @Test
    public void testBatchUpdateBeans() {
        List<User> users = new ArrayList<>();
        int size = 100;
        for (int i = 0; i < size; i++) {
            User user = new User("zero" + i, "zero@example.com", "123456");
            user.updatedTime = new Date();
            users.add(user);
        }
        BatchUpdateResult result1 = conn.batchInsert(users);
        Assert.assertEquals(users.size(), result1.affectedRows);
        for (User user : users) {
            Assert.assertNotNull(user.id);
            user.username += ".changed";
        }
        BatchUpdateResult result2 = conn.batchUpdate(users);
        Assert.assertEquals(users.size(), result2.affectedRows);
        List<User> list = conn.listAll(User.class);
        for (User user : list) {
            Assert.assertTrue(user.username.endsWith(".changed"));
        }
    }

    ///////////////////////////////////////////Use Sql Provider////////////////////////////////////////////////////
    @Test
    public void testExecuteExternalSql() {
        String first = conn.query("#all").first(RowMapper.STRING);
        System.out.println(first);
    }

    @Test
    public void testExecuteExternalSql2() {
        User first = conn.query("#user.login", "zero", "123456").first(User::new);
        Assert.assertNull(first);
    }


    @Test
    public void testLocalDate() {
        int i = conn.insertMap("users", row -> {
            row.put("username", "Frank");
            row.put("email", "frank@example.com");
            row.put("password", "123456");
            row.put("gender", Gender.MALE);
            row.put("updated_time", LocalDateTime.of(2019, 12, 31, 12, 5));
        });
//        conn.commit();
        Assert.assertEquals(1, i);
        LocalDate from = LocalDate.of(2019, 12, 25);
        LocalDate to = LocalDate.of(2020, 1, 1);
        Assert.assertFalse(conn.query("select * from users where updated_time BETWEEN ? AND ?", from, to).isEmpty());
    }

    @Test
    public void testTransaction() {
        SqlConnection[] ss = new SqlConnection[1];
        Sql.transaction(conn -> {//TODO bug, 连接已关闭
            ss[0] = conn;
            System.out.println(conn.connection().hashCode());
            System.out.println(ss[0].getAutoCommit());
            return true;
        });
        for (int i = 0; i < 5; i++) {
            SqlConnection conn = Sql.open();
            System.out.println(conn.connection().hashCode());
            System.out.println(conn.getAutoCommit());
            conn.close();
        }
    }

    //    @Test
    public void testTransaction2() {
        CompletableFuture.runAsync(() -> {
            Sql.transaction(conn -> {
                try {
                    Thread.sleep(10_000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return true;
            });
        });
        for (int i = 0; i < 5; i++) {
            System.out.println("正在获取连接: " + LocalDateTime.now());
            try (SqlConnection conn = Sql.open()) {
                System.out.println("获取连接: " + LocalDateTime.now());
                System.out.println(conn.connection().hashCode());
                System.out.println(conn.getAutoCommit());
            }
        }
    }
/*
    @Test
    public void testTransaction3() {
        SqlConnection conn = SqlTx.begin();
        try {
            User user = new User("May", "may@example.com", "123456");
            conn.insert(user);
            conn.insert(user);
            conn.insert(user, "xxx");
        } catch (Exception e) {
            conn.rollback();
        }
        List<User> list = conn.list(User.class, Where.EMPTY);
        Assert.assertTrue(list.isEmpty());
        conn.close();
        SqlConnection conn2 = SqlTx.begin();
        Assert.assertNotSame(conn2, conn);
        conn2.close();
    }*/

    //////////////////////////////////////////////////////////////////////////////

    public void test() {
        conn.statement(statement -> {
//            statement.getUpdateCount()
        });
    }
}