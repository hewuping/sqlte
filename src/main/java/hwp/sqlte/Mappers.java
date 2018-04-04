package hwp.sqlte;

import hwp.sqlte.mapper.PublicBeanMapper;

import java.util.function.Supplier;

/**
 * @author Zero
 *         Created on 2018/4/4.
 */
public class Mappers {

    public static <T> RowMapper<T> newPBM(Supplier<T> supplier) {
        return new PublicBeanMapper<>(supplier);
    }



}
