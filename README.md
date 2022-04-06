# sqlte

[![Java CI with Gradle](https://github.com/hewuping/sqlte/actions/workflows/ci.yml/badge.svg)](https://github.com/hewuping/sqlte/actions/workflows/ci.yml)


Database|Minimum driver version
--|--
MySQL|5.1.37
H2|1.4.197

https://mvnrepository.com/artifact/mysql/mysql-connector-java

## install
```
./gradlew build
./gradlew publishToMavenLocal

```

```
 compile group: 'hwp.sqlte', name: 'sqlte', version: 'x.x.x'
```

**use jitpack**

Maven
```
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
<dependencies>
    <dependency>
        <groupId>com.github.hewuping</groupId>
        <artifactId>sqlte</artifactId>
        <version>0.2.8</version>
    </dependency>
</dependencies>
```
Gradle
```
repositories {
    mavenCentral()
    maven { url "https://jitpack.io" }
}
dependencies {
    implementation 'com.github.hewuping:sqlte:0.2.8'
}
```

## SqlConnection

```
@Table(name = "users")
public class User {

    @Id(generate = true)
    public Integer id;
    public String username;
    public String email;
    public String password;
    public String password_salt;
    ...
}
```
**Insert One**

```
User user = new User("May", "may@xxx.com", "123456");
conn.insert(user);
conn.insert(user, "users");
```

**Batch insert**
```
conn.batchUpdate("INSERT INTO users (email, username)  VALUES (?, ?)", executor -> {
    executor.exec("bb@example.com", "bb");
    executor.exec("aa@example.com", "aa");
});
```
OR
```
conn.batchInsert("users", "email, username", executor -> {
    executor.exec("bb@example.com", "bb");
    executor.exec("aa@example.com", "aa");
});
```
OR
```
List<User> users = new ArrayList<>();
int size = 20;
for (int i = 0; i < size; i++) {
    User user = new User("zero" + i, "zero@xxx.com", "123456");
    user.id = i;
    user.password_salt = "***";
    users.add(user);
}
conn.batchInsert(users, "users");
```

**Query by ID**
```
User user = conn.load(User::new, 123);
```

**Query first**
```
SqlConnection conn = Sql.open();
User user = conn.query("select * from users where email=? limit 1", "xxx@xxx.com").first(User::new);
conn.close();
```

**Delete**

```
conn.delete(user)
```

**Update**

```
conn.update("users", row -> {
    row.set("username", "zero00").set("email", "zero@example.com");
}, where -> {
    where.and("id = ?", 123);
});
```

```
user.uername = "new name";
conn.update(user, "username", true);
```

## SqlBuilder

```
SqlBuilder sql = new SqlBuilder();
sql.from("users"); // select * from users
sql.where(where -> {
    if ("zero".startsWith("z")) {
        where.and("username=?", "zero");
    }
    where.and(Condition.startWith("username", "Z"));
    where.and("password=?", "123456");
    where.and(Condition.in("age", 12, 13, 15, 17));
});
sql.groupBy("age", having -> {
    having.and("age < ?", 18);
    having.and(Condition.eq("username", "Zero"), Condition.eq("username", "Frank"));
});
sql.orderBy(order -> {
    order.asc("username");
    order.desc("age");
});
sql.limit(1, 20);

List<User> users = conn.query(sql).list(User::new);
```


## QuerySql

```
QuerySql sql = new QuerySql();
sql.select("*").from("user").where(where -> {
    where.and("created_at > ?", new Date());
    where.and("age > ?", 10);
}).groupBy("uid").orderBy("name desc");
```