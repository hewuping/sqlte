package hwp.sqlte.example;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;

@Documented
@Target({FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Gte {

    /**
     * 列名, 优先级高于 {@link hwp.sqlte.Column } @Column 和 字段属性名
     *
     * @return
     */
    String value() default "";

}
