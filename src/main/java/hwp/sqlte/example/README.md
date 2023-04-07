# Example 查询

## 初级

查询数据, 并返回 List

```java
List<User> list = conn.listExample(new User("username"));
List<User> list = conn.listExample(User.class, new XXX("username"));//XXX 为任意对象, 只要确保属性名存在查询的表中

```

判断数据是已存在

```java
conn.contains(new User("username")); 
```

## 高级

```java
// 这里继承 PageQuery 不是必需的, 如果查询需要分页推荐继承 
// 建议将前端参数(JSON)直接转为 Java 对象用于查询
public class UserQuery extends PageQuery {

    @Like(columns = {"username", "nickname"})// 模糊查询, 同时查询两个字段
    public String name;

    public Range<Integer> age; // Range 表示范围搜索, 使用 SQL 的 BETWEEN 关键字, 如果其一为 null, 则自动转换为 >= 或 <= 

    @Column(name = "deleted") // 如果表列名与字段名不一致, 可以设置指定列名
    public boolean deleted;

    @Ignore // 忽略该字段
    public String temp;

}
```

注解说明: 

- @Like 模糊查询 %q%
- @StartWith 模糊查询 q%
- @EndWith 模糊查询 %q
- @Gt 大于
- @Gte 大于等于
- @Lt 小于
- @Lte 小于等于

分页

```java
Page<User> page = conn.queryPage(sql->{
    // 第一个 query 用于构建查询条件, 第二个 query 用于分页(因为继承了 PageQuery, 如果没有继承则分别传递 offset 和 limit)
    sql.select(User.class).where(query).paging(query);
},User.class);
```

不分页

```java
List<User> list = conn.query(sql->{
    sql.select(User.class).where(query);
},User.class);
```

或者

```java
List<User> list = conn.listExample(User.class, query);
```