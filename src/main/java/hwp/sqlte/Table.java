package hwp.sqlte;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

/**
 * @author Zero
 * Created on 2018/4/4.
 */
@Documented
@Target({TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {

    String name();

    String schema() default "";

}
