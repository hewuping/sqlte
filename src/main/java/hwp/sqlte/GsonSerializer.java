package hwp.sqlte;

import com.google.gson.Gson;

public class GsonSerializer implements JsonSerializer {
    private final Gson gson;

    public GsonSerializer() {
        gson = new Gson();
    }

    public GsonSerializer(Gson gson) {
        this.gson = gson;
    }

    @Override
    public <T> T fromJson(String json, Class<T> aClass) {
        return gson.fromJson(json, aClass);
    }

    @Override
    public String toJson(Object o) {
        return gson.toJson(o);
    }


}