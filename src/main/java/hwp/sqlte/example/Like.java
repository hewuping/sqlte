package hwp.sqlte.example;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;

/**
 * 模糊查询: %query%
 */
@Documented
@Target({FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Like {
}
