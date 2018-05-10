package hwp.sqlte;

import hwp.sqlte.mapper.IntMapper;
import hwp.sqlte.mapper.PublicBeanMapper;

import java.util.function.Supplier;

/**
 * @author Zero
 *         Created on 2018/4/4.
 */
public class Mappers {

    public static <T> RowMapper<T> bean(Supplier<T> supplier) {
        return new PublicBeanMapper<>(supplier);
    }

    public static IntMapper integer() {
        return IntMapper.MAPPER;
    }

}
