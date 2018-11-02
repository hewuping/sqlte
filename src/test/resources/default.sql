-- 定义: ID定义格式`-- #<id>` #号前面有一个空格, #号后面的字符串为ID, 以空行分隔
-- 调用: <文件名去掉扩展名>.id,

-- key:version
-- #version
SELECT version()

-- key: all
-- #all
SELECT * FROM  users

-- key: login
-- eg. conn.query("#login", "Cindy", "123456")
-- #login
SELECT * FROM users WHERE username=? and password=?

-- #multi-line-sql
SELECT
	*
FROM
	orders
LEFT JOIN users ON orders.user_id = users.id
WHERE
	users.username = ?