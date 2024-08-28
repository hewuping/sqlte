# sqlte

[![Java CI with Gradle](https://github.com/hewuping/sqlte/actions/workflows/ci.yml/badge.svg)](https://github.com/hewuping/sqlte/actions/workflows/ci.yml)


Database|Minimum driver version
--|--
MySQL|5.1.37
H2|1.4.197

https://mvnrepository.com/artifact/mysql/mysql-connector-java

## install

```bash
./gradlew build
./gradlew publishToMavenLocal
```

```groovy
 compile group: 'com.github.hewuping', name: 'sqlte', version: 'x.x.x'
```

**use jitpack**

Maven
```xml
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
        <version>0.2.26</version>
    </dependency>
</dependencies>
```
Gradle
```groovy
repositories {
    mavenCentral()
    maven { url "https://jitpack.io" }
}
dependencies {
    implementation 'com.github.hewuping:sqlte:0.2.26'
}
```

## 注意事项

- 字段必须使用 `public` 声明, 否则字段会被忽略, 不使用 `get`/`set` 方法
- 这不是 ORM 框架, 这里用到的都是表的的列名, 而不是类属性名

## Example

```java
var config = Sql.config();
config.setDataSource(dataSource);
```

```java
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

```java
User user = new User("May", "may@xxx.com", "123456");
conn.insert(user); // 插入到默认表
conn.insert(user, "users_1"); // 插入到特定表
```

**Batch insert**
```java
conn.batchUpdate("INSERT INTO users (email, username)  VALUES (?, ?)", executor -> {
    executor.exec("bb@example.com", "bb");
    executor.exec("aa@example.com", "aa");
});
```
OR
```java
conn.batchInsert("users", "email, username", executor -> {
    executor.exec("bb@example.com", "bb");
    executor.exec("aa@example.com", "aa");
});
```
OR
```java
List<User> users = new ArrayList<>();
// ...
conn.batchInsert(users); // 插入到默认表
conn.batchInsert(users, "users_1"); // 插入到特定表
```

**Query by ID**
```
User user = conn.tryGet(User.class, 123); // 不存在返回 null

User user = conn.mustGet(User.class, 123); // 不存在抛异常
```

**Query first**
```java
conn.query("select * from users where email=? limit 1", "xxx@xxx.com").first(User::new);

conn.first(User.class, user -> { user.username="xxx"; });

conn.firstExample(new User("username"))
```

**Query list**

```java
conn.query(sql).list(User.class);// by sql string
conn.query(sql->{}).list(User.class); // by SqlBuilder

conn.list(User.class, where -> {});
conn.list(User.class, List.of(100, 101, 102));// by ID

conn.listExample(User.class, query->{});// by example
conn.listExample(User.class, new UserQuery());// by example
conn.listExample(new User());// by example
```

**Query by Example**

```java
class User {
    public Integer id;
    public String name;
    public Integer deposit;
    public Integet age;
    public String status;
    //...
}

// UserQuery 可以很方便的作为 API 的查询参数(POST + JSON)
class UserQuery {
    public Range<Integer> id = new Range<>(10, 30);// id BETWEEN 10 AND 30
    @StartWith
    public String name = "z";// name LIKE "z%"
    @Lte
    public Integer deposit = 1000;// deposit <= 1000
    public Integer[] age = new Integer[]{16, 18, 20}; // age IN (16, 18, 20)
    public String status = "Active"; // status = "Active"
}

List<User> list = conn.query(sql->sql.select(User.class).where(new UserQuery())).list(User.class);

// sql:  SELECT * FROM user WHERE (id BETWEEN ? AND ?) AND name LIKE ? AND deposit <= ? AND age IN (?, ?, ?) AND status = ?
// args: [10, 30, z%, 1000, 16, 18, 20, Active]

conn.query(sql->sql.select(User.class).where(new User("Active"))).list(User.class);
conn.query(sql->sql.select(User.class).where(new User("Active"))).list(UserVo.class);
```


**Delete**

```java
conn.delete(user)
conn.delete(user, "table_name")
conn.delete(User.class, where->{})
conn.delete("table_name", where->{})
conn.deleteByExample(example)
```

**Update**

```java
user.uername = "new name";
conn.update(user);
```

```java
// Update specified fields
conn.update(user, "column1, column2, column3...");// true: ignoreNullValue
// Update specified fields and ignore null values
conn.update(user, "column1, column2, column3...", true);

conn.update("table_name", map, where->{});

conn.update("users", row -> {
    row.set("username", "zero00").set("email", "zero@example.com");
}, where -> {
    where.and("id = ?", 123);
});

// For more update operations, see SqlConnection.java
```

**Batch Update**

```java
List<User> users=...
conn.batchUpdate(users)
conn.batchUpdate(users, "table_name")
conn.batchUpdate(users, null, "column1, column2, column3...")
```



## SqlBuilder

```java
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

`Where` 动态构建查询条件

```java
sql.where(where -> {
    if (username != null) {
        where.and("username=?", username);
    }
    if (deleted) {
        where.and("deleted=?", deleted);
    }
});
// 这里提供更优雅的写法
sql.where(where -> {
    // 如果参数值为 null 或 空字符串, 该查询条件会被移除
    where.andIf("username=?", username); // 推荐
    // where.andIf("username=?", username, StringUtils::isNotBlank);// 同上
    // where.and(StringUtils.isNotBlank(username), "username=?", username)// 同上
    where.andIf("deleted=?", deleted); // deleted 为 null 时, 该查询条件会被移除
});

// or() 和 orIf() 类似
```

`Where` 类的其他方法

```java
where.or(Condition... conditions) // OR (xxx OR xxx OR xxx)
where.andOr(Condition... conditions) // AND (xxx OR xxx OR xxx)
where.orAnd(Condition... conditions) // OR (xxx AND xxx AND xxx)
where.and(Map<String, ?> map)  // AND (key1=value1 AND key2=value2 AND key3=value3)
where.of(Object example) // 根据对象生成查询条件, 忽略值为 null 或 空字符串的字段
...
```

## QuerySql

```java
QuerySql sql = new QuerySql();
sql.select("*").from("user").where(where -> {
    where.and("created_at > ?", new Date());
    where.and("age > ?", 10);
}).groupBy("uid").orderBy("name desc");
```

## Pageable 接口

```java
public interface Pageable {

    int getPage();

    int getPageSize();
    
}
```
`PageQuery` 是一个实现 `Pageable` 接口, 并支持排序的工具类, 也可以自己实现 `Pageable`

例子：创建一个 GlossaryQuery 类接收前端查询参数

```java
public class GlossaryQuery extends PageQuery {

    @Like(columns = {"name", "description"}) // 查询参数名为 name, 但是对 name 和 description 列进行模糊查询
    public String name;
    public String srcLang; // 精确查询 (src_lang = ?)
    public String trgLang;
    public Integer userId;
    public Integer domainId;
    @Gt
    public Integer size; // 范围查询 (size > ?)
    public Range<Date> createdAt; // 范围查询 (BETWEEN ? AND ?)
    // 其他注解: @StartWith, @EndWith, @Gte, @Lt, @Lte
}

public Page<Glossary> getList(GlossaryQuery query) {
    return db.queryPage(sql -> {
        // 查询条件由 GlossaryQuery 类中定义的字段和注解生成
        sql.select(Glossary.class).where(query);
        // PageQuery 类中包含一个可选参数 sort，用于实现对指定字段进行排序 
        sql.orderBy(order -> {
            Sort sort = query.getSort();
            order.by("name", sort.get("name")); // 如果为 null 则忽略 (建议设置默认排序)
            // created_at 为表列名, 必须一致
            // createdAt 为接收前端传入值的参数名, 为自定义名称
            order.by("created_at", sort.getOrDefault("createdAt", Direction.ASC)); // 如果为 null 则使用 升序
            // order.by("created_at", sort.asc("createdAt")); // 同上, 默认使用升序
            // order.asc("created_at", sort.get("createdAt")); // 同上, 默认使用升序
            // order.asc("created_at", query.getSort("createdAt")); // 同上, 默认使用升序 (推荐)
        });
        sql.paging(query); // sql.limit(query.getPage(), query.getPageSize()) 的简写
    }, Glossary::new);
}
```


## Spring Integration

```java
@Bean
public SqlteTemplate sqlteTemplate(DataSource dataSource) {
    var config = Sql.config();
    // config.setJsonSerializer(new JacksonSerializer());
    config.setDataSource(dataSource);

    return new SqlteTemplate() {
        @Override
        protected Connection open(DataSource dataSource) {
            return DataSourceUtils.getConnection(dataSource);
        }

        @Override
        protected void close(Connection connection) {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    };
}
```