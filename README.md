# sqlte

[![Java CI with Gradle](https://github.com/hewuping/sqlte/actions/workflows/ci.yml/badge.svg)](https://github.com/hewuping/sqlte/actions/workflows/ci.yml)


| Database | Minimum driver version |
|----------|------------------------|
| MySQL    | 5.1.37                 |
| H2       | 1.4.197                |

https://mvnrepository.com/artifact/mysql/mysql-connector-java

## Install from Source

```bash
./gradlew build
./gradlew publishToMavenLocal
```

```groovy
implementation group: 'com.github.hewuping', name: 'sqlte', version: '0.3.1'
```

## Install from Maven

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
    <version>0.3.1</version>
</dependency>
</dependencies>
```

## Install from Gradle

```groovy
repositories {
    mavenCentral()
    maven { url "https://jitpack.io" }
}
dependencies {
    implementation 'com.github.hewuping:sqlte:0.3.1'
}
```

## 注意事项

- 字段必须使用 `public` 声明, 否则该字段会被忽略, 不使用 `get`/`set` 方法
- 不同于 ORM 框架, `sqlte` 构建 SQL 时使用的是表的列名, 而不是类属性名

# Tutorial

```java
var config = Sql.config();
config.setDataSource(dataSource);
SqlConnection conn = Sql.open();
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

## 插入数据

**Insert One**

```java
User user = new User("May", "may@xxx.com", "123456");
conn.insert(user); // 插入到默认表
conn.insert(user, "users_1"); // 插入到特定表
```

**Batch insert**
```java
// 更接近原生的写法
conn.batchUpdate("INSERT INTO users (email, username)  VALUES (?, ?)", executor -> {
    executor.exec("bb@example.com", "bb");
    executor.exec("aa@example.com", "aa");
});

// 简化后的写法
conn.batchInsert("users", "email, username", executor -> {
    executor.exec("bb@example.com", "bb");
    executor.exec("aa@example.com", "aa");
});

// 基于映射对象的写法
List<User> users = new ArrayList<>();
// ...
conn.batchInsert(users); // 插入到默认表
conn.batchInsert(users, "users_1"); // 插入到特定表
```

## 删除数据

```java
conn.delete(user)
conn.delete(user, "table_name")
conn.delete(User.class, where->{})
conn.delete("table_name", where->{})
conn.deleteByExample(example)
conn.executeUpdate(String sql, args)
```

## 更新数据

基于对象模型更新数据
```java
user.uername = "new name";
conn.update(user);

// Update specified fields
conn.update(user, "column1, column2, column3...");

// Update specified fields and ignore null values
conn.update(user, "column1, column2, column3...", true); // ignoreNullValue = true
```

```java
// Use map
map.put("foo1", "bar1");
map.put("foo2", "bar2");
conn.update("table_name", map, where->{});

// Use lambda
conn.update("users", row -> {
    // row.set("foo1", "bar2").set("foo2", "bar2");
    row.set("foo1", "bar1");
    row.set("foo2", "bar2");
}, where -> {
    where.and("id = ?", 123);
});

// For more update operations, see SqlConnection.java
```

基于SQL更新/删除
```java
conn.executeUpdate(String sql, args)
conn.executeUpdate(sql -> {
    
})
```

**Batch Update**

"批次更新"跟"单次更新多条记录"是有区别的, 批次更新是多条sql, 但是参数值不一样.

```java
// 基于对象模型更新
List<User> users=...
conn.batchUpdate(users)
conn.batchUpdate(users, "table_02") // 特定表
conn.batchUpdate(users, "table_02", "column1, column2, column3...") // 特定表, 且仅更新指定的列 

// 基于SQL, 该例子效果同 IN 关键字
db.batchUpdate("UPDATE users SET status='RESET_REQUIRED' WHERE id=?", executor -> {
    for (Integer id : userIds) {
        executor.exec(id);
    }
});
```

## 查询数据

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


### SqlBuilder 的用法

```java
SqlBuilder sql = new SqlBuilder();
sql.from("users"); // select * from users
sql.where(where -> {
    where.and(Condition.startWith("username", "Z"));//同: where.and("username LIKE ?", "Z%")
    where.and(Condition.eq("password", "123456"));//同: where.and("password=?", "123456")
    where.and(Condition.in("age", 12, 15, 18));//同: where.and("age in (?,?,?)", 12, 15, 18)
});
sql.groupBy("age", having -> {
    having.and(Condition.lt("age", 18));//同: where.and("age<?", 18)
    having.andOr(Condition.eq("username", "Zero"), Condition.eq("username", "Frank"));
});
sql.orderBy(order -> {
    order.asc("username");
    order.desc("age");
});
sql.limit(1, 20);

List<User> users = conn.query(sql).list(User::new);
```
虽然 `SqlBuilder` 中提供了 `select` `from` 等关键字方法, 但不强制使用, 很多时候 Java 文本块会是更好的选择

比如:
```java
SqlBuilder sql = new SqlBuilder();
// 在 Navicat 中写好的SQL直接复制过来
sql.sql("""
    SELECT orders.order_id, customers.customer_name, orders.order_date
    FROM orders
    INNER JOIN customers ON orders.customer_id=customers.customer_id""");
// 如果查询条件也不需要动态构建, WHERE 也可以直接添加到上面的文本块中 
sql.where(where -> {
    // ...
});
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
// 这里提供更简单优雅的写法 andIf(), 减少不为空的判断
sql.where(where -> {
    // 当参数值为空时(包括: null, ""), 忽略该查询条件
    where.andIf("username=?", username); // 推荐
    // where.andIf("username=?", username, StringUtils::isNotBlank);// 同上
    // where.and(StringUtils.isNotBlank(username), "username=?", username)// 同上
    where.andIf("deleted=?", deleted); // deleted 为 null 时, 该查询条件会被移除
});

// or() 和 orIf() 类似 and() 和 andIf()
```

`Where` 类的其他方法

```java
where.or(Condition... conditions) // OR (xxx OR xxx OR xxx)
where.andOr(Condition... conditions) // AND (xxx OR xxx OR xxx)
where.orAnd(Condition... conditions) // OR (xxx AND xxx AND xxx)
where.and(Map<String, ?> map)  // AND (key1=value1 AND key2=value2 AND key3=value3)
where.of(Object example) // 根据对象生成查询条件, 会忽略值为 null 或 空字符串的字段
...
```

`CTE` 的用法
```sql
WITH customer_orders AS (
  SELECT customer_id, 
         COUNT(*) AS order_count, 
         SUM(total_amount) AS total_amount, 
         AVG(total_amount) AS avg_amount
  FROM orders
  GROUP BY customer_id
),
ranked_customers AS (
  SELECT c.name, o.avg_amount, 
         DENSE_RANK() OVER (ORDER BY o.avg_amount DESC) AS rank
  FROM customer_orders o 
  JOIN customers c ON o.customer_id = c.id
)
SELECT name, avg_amount
FROM ranked_customers
WHERE rank <= 5;
```

```java
conn.query(sql -> {
    // 当 with 中的查询不需要动态构建查询条件时, 本人建议使用 Java 文本块构建 SQL
    sql.with(cte -> {
        cte.set("customer_orders", sb -> {
            //SELECT customer_id,
            //       COUNT(*) AS order_count,
            //       SUM(total_amount) AS total_amount,
            //       AVG(total_amount) AS avg_amount
            //FROM orders
            //GROUP BY customer_id
        });
        cte.set("ranked_customers", sb -> {
            //SELECT c.name, o.avg_amount,
            //       DENSE_RANK() OVER (ORDER BY o.avg_amount DESC) AS rank
            //FROM customer_orders o
            //JOIN customers c ON o.customer_id = c.id
        });
    });
    sql.select("name, avg_amount");
    sql.from("ranked_customers");
    sql.where(where -> {
        where.and("rank <= ?", 5);
    });
});
```

### Query 的用法

建议使用功能更完善的 `SqlBuilder`

```java
Query sql = new Query();
sql.select("*").from("user").where(where -> {
    where.and("created_at > ?", new Date());
    where.and("age > ?", 10);
}).groupBy("uid").orderBy("name desc");
```

## 其他

### Pageable 接口

```java
public interface Pageable {
    int getPage();
    int getPageSize();
}

public class PageQuery implements Pageable {
    private Sort sort = new Sort();
    private int page = 0;
    private int pageSize = 10;
    // ...
}    
```
`PageQuery` 是一个实现 `Pageable` 接口并支持排序的工具类，主要是为了统一分页接口编码规范。

例子：创建一个 UserQuery 类接收前端查询参数, 虽然是查询操作, 但还是推荐 POST + JSON Body 进行前后端交互

```java
public class UserQuery extends PageQuery {

    @Like(columns = {"username", "nickname"}) // 查询参数名为 name, 但是对 username 和 nickname 列进行模糊查询
    public String name;
    public String email; // 精确查询 (email = ?)
    @Gt
    public Integer age; // 范围查询 (age > ?)
    public Range<Date> createdAt; // 范围查询 (BETWEEN ? AND ?)
    // 其他注解: @StartWith, @EndWith, @Gte, @Lt, @Lte
}

public Page<User> getList(UserQuery query) {
    return db.queryPage(sql -> {
        // 查询条件由 UserQuery 类中定义的字段和注解生成
        sql.select(User.class).where(query);// select * from users
        // PageQuery 类中包含一个可选参数 sort，用于实现对指定字段进行排序 
        sql.orderBy(order -> {
            Sort sort = query.getSort();
            order.by("age", sort.get("age")); // 如果为 null 则忽略 (建议设置默认排序)
            // created_at 为表列名, 必须一致
            // createdAt 为接收前端传入值的参数名, 为自定义名称
            order.by("created_at", sort.getOrDefault("createdAt", Direction.ASC)); // 如果为 null 则使用 升序
            // order.by("created_at", sort.asc("createdAt")); // 同上, 默认使用升序
            // order.asc("created_at", sort.get("createdAt")); // 同上, 默认使用升序
            // order.asc("created_at", query.getSort("createdAt")); // 同上, 默认使用升序 (推荐)
        });
        sql.paging(query); // sql.limit(query.getPage(), query.getPageSize()) 的简写
    }, User::new);// 查询到的数据转为 User 对象, 这里也可以是其他类, 比如 UserVo
}
```

前端例子
```ts
export interface Page {
  data: never[];
  rowCount: number;
}

export interface PageQuery {
  page?: number,
  pageSize?: number;
  sort?: { [key: string]: 'ASC' | 'DESC' };
  [key: string]: any;
}

export function getUsers(query: PageQuery): Promise<Page> {
  return http.post("/api/users", query)
}

let query: PageQuery = reactive({
  name: "frank",
  email: "",
  page: 1,
  pageSize: 30,
  sort:{
    "createdAt": "DESC"
  }
});

getUsers(query).then(rs=>{})
```

### 一对多与多对一

本库不提供`@ManyToOne`和`@OneToMany`映射实体关系，所以查询也不会自动查询关联数据。

可以使用下列方法实现类似功能：
```java
conn.queryPage(UserGroup.class, sql -> {
    sql.select(UserGroup.class).paging(1, 10);
}, (userGroup, row) -> {
    userGroup.users = getUsersByGroupId(userGroup.id);
});
```

## Json Serializer

配置 `JsonSerializer`，使得数据存储时，将字段值转为 JSON。

注意：由于Java的泛型不是真正的泛型，在JSON反序列化时可能会出现问题。 也许使用数组代替集合会是更好的选择。

```java
var config = Sql.config();
config.setJsonSerializer(new JacksonSerializer());
```

```java
public class GsonSerializer implements JsonSerializer {
    private final Gson gson;

    public GsonSerializer() {
        this.gson = new Gson();
    }

    public GsonSerializer(Gson gson) {
        this.gson = gson;
    }

    public <T> T fromJson(String json, Class<T> aClass) {
        return this.gson.fromJson(json, aClass);
    }

    public String toJson(Object o) {
        return this.gson.toJson(o);
    }
}
```

```java
public class JacksonSerializer implements JsonSerializer {

    private ObjectMapper instance = new ObjectMapper();

    public JacksonSerializer() {
        mapper.registerModule(new SimpleModule());
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);//ISO
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        // 其他配置
    }

    @Override
    public <T> T fromJson(String json, Class<T> classOfT) {
        try {
            return instance.readValue(json, classOfT);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toJson(Object src) {
        try {
            return instance.writeValueAsString(src);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
```

用法

```java
public class Foo {
    public String f1;
    public String f2;
}

@Table(name="bar")
public class Bar {
    @Column(json=true)
    public Foo foo;
}
// 存储 Bar 时, foo 的值会被转为 JSON
```

## Spring Integration

`SqlteTemplate` 实现了 `SqlConnection` 接口, 但是每次查询/更新操作都会通过`open()`获取连接, 执行完后自动关闭连接,
这里将两个方法的实现委托给 Spring JDBC  DataSourceUtils 对应的方法, 以便使用 Spring 的事务管理

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

