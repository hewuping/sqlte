package hwp.sqlte;

import java.io.Serializable;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;

/**
 * @author Zero
 * Created on 2018/4/4.
 */
@Documented
@Target({FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {

    String name() default "";

    boolean json() default false;

    boolean update() default true;

    Class<? extends Converter<?, ? extends Serializable>> converter();

}
