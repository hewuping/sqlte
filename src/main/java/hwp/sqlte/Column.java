package hwp.sqlte;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.lang.annotation.ElementType.FIELD;

/**
 * @author Zero
 * Created on 2018/4/4.
 */
@Documented
@Target({FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {

    String name();

    Class<? extends Converter> serializer() default Converter.class;

//    JDBCType jdbcType() default JDBCType.JAVA_OBJECT;//@jdbcType=Date.class NULL

}
