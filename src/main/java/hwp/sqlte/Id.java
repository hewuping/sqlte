package hwp.sqlte;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;

/**
 * @author Zero
 *         Created by Zero on 2017/8/13 0013.
 */
@Documented
@Target({FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Id {

    String column() default "";

    boolean auto() default false;

}
