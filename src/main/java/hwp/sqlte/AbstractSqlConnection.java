package hwp.sqlte;

import hwp.sqlte.util.ClassUtils;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class AbstractSqlConnection implements SqlConnection {

    public <T> List<T> query(Class<T> returnType, Consumer<Where> where) throws SqlteException {
        return query(sql -> sql.from(getTableName(returnType)).where(where)).list(returnType);
    }

    @Override
    public <T> List<T> list(Class<T> clazz, Consumer<Where> consumer) {
        return query(sql -> sql.from(getTableName(clazz)).where(consumer)).list(clazz);
    }

    @Override
    public <T> List<T> list(Class<T> clazz, Collection<? extends Serializable> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        ClassInfo info = getClassInfo(clazz);
        String pkColumn = info.getPKColumn();
        return list(clazz, where -> {
            where.and(Condition.in(pkColumn, ids));
        });
    }

    @Override
    public <T> T first(Class<T> clazz, Consumer<T> consumer) {
        Objects.requireNonNull(clazz);
        Objects.requireNonNull(consumer);
        ClassInfo info = getClassInfo(clazz);
        T query = ClassUtils.newInstance(clazz);
        consumer.accept(query);
        return query(sql -> sql.from(info.getTableName()).where(query).limit(1)).first(clazz);
    }

    @Override
    public <T> T first(Supplier<T> supplier, Consumer<T> consumer) {
        Objects.requireNonNull(supplier);
        Objects.requireNonNull(consumer);
        T example = supplier.get();
        consumer.accept(example);
        return query(sql -> sql.from(getTableName(example.getClass())).where(example).limit(1)).first(supplier);
    }

    @Override
    public <T> T firstExample(T example) {
        Objects.requireNonNull(example, "example can't be null");
        Class<T> clazz = (Class<T>) example.getClass();
        return query(sql -> sql.from(getTableName(clazz)).where(example).limit(1)).first(clazz);
    }

    @Override
    public <T> List<T> listExample(T example) {
        Class<T> aClass = (Class<T>) example.getClass();
        return list(aClass, where -> where.of(example));
    }

    @Override
    public <T> List<T> listExample(Class<T> clazz, Consumer<T> consumer) {
        T example = ClassUtils.newInstance(clazz);
        consumer.accept(example);
        return listExample(example);
    }


    @Override
    public <T> int delete(Class<T> clazz, Consumer<Where> whereConsumer) throws SqlteException {
        return this.delete(getTableName(clazz), whereConsumer);
    }

    @Override
    public <T> int deleteByMap(Class<T> clazz, Consumer<Map<String, Object>> whereConsumer) throws SqlteException {
        Map<String, Object> map = new LinkedHashMap<>();
        whereConsumer.accept(map);
        return this.delete(getTableName(clazz), where -> where.and(map));
    }

    protected String getTableName(Class<?> clazz) {
        ClassInfo info = ClassInfo.getClassInfo(clazz);
        return info.getTableName();
    }

    protected ClassInfo getClassInfo(Class<?> clazz) {
        return ClassInfo.getClassInfo(clazz);
    }

}
