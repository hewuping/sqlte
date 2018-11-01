package hwp.sqlte.mapper;


import hwp.sqlte.Row;
import hwp.sqlte.RowMapper;

import java.util.Objects;

/**
 * @author Zero
 *         Created on 2017/3/20.
 */
public class StringMapper implements RowMapper<String> {

    public static final StringMapper MAPPER = new StringMapper();

    private StringMapper(){}

    @Override
    public String map(Row row) {
        return Objects.toString(row.values().iterator().next());
    }
}