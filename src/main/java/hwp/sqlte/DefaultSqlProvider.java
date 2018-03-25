package hwp.sqlte;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

/**
 * @author Zero
 *         Created by Zero on 2017/7/12 0012.
 */
public class DefaultSqlProvider implements SqlProvider {


    private String[] fileNames;
    Logger logger = LoggerFactory.getLogger("sql");

//    private Map<String,String>

    public DefaultSqlProvider(String... fileNames) {
        this.fileNames = fileNames;
        for (String name : fileNames) {
            try {
                try (InputStream in= DefaultSqlProvider.class.getClassLoader().getResourceAsStream(name)){

                }
            } catch (IOException e) {
                e.printStackTrace();
                logger.warn("",e);
            }
        }
    }

    @Override
    public String getSql(String sqlOrKey) {
        return null;
    }

}
