
```
D.tx(tx->{
//session.getConnction();
//PreparedStatment statment= conn.preparedStatment();
session=Session.getSession();
session.tx(session->{

});
Session temSession=Session.getSession(ds)
...
temSession.close();//临时session需要释放资源

session.triggerError()
session.

session.insert(table,map)
session.insert(bean)
session.batchInsert(table,mapList)
session.batchInsert(table,culumns,users)
session.update(sql)
session.update(sql,args...)
session.update(sql,mapArgs)
session.update(sql,mapArgs).where(w->{
   if(username != null) w.append("username=?",username);
   if(age != null) w.append("age<?",age);
});
session.query("select * from user where username=?","zero").first(User.MAPPER)
session.query("select * from user where username=? and password=?","zero","123456").first(User.MAPPER)
session.query("select * from user where username=?","zero").list(User.MAPPER)
session.query("select * from user where username=?").args("zero").exec().map(User.MAPPER)
session.query("select * from user").where(w->{
    if(username != null) w.append("username=?",username);
    if(age != null) w.append("age<?",age);
}).cache(10s).exec().map(User.MAPPER);



//
if(session.isInTx()){

}
})
```