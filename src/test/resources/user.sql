-- 定义: ID定义格式`-- #<id>` #号前面有一个空格, #号后面的字符串为ID, 以空行分隔
-- 调用: <文件名去掉扩展名>.id,


-- key is user.all
-- #all
SELECT * FROM  user

-- key is user.login
-- eg. conn.query("#user.login", "Cindy", "123456")
-- #login
SELECT * FROM users WHERE username=? and password=?

