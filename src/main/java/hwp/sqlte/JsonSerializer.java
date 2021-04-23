package hwp.sqlte;

/**
 * @author Zero
 * Created on 2021/4/21.
 */
public interface JsonSerializer {

    public <T> T fromJson(String json, Class<T> classOfT);

    public String toJson(Object src);

}
