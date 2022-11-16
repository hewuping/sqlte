package hwp.sqlte;

import hwp.sqlte.example.Like;

import java.util.function.Consumer;
import java.util.function.Function;

public class UserQuery implements Function<Consumer<UserQuery>, UserQuery> {


    @Like(columns = {"name1", "name2"})
    public String name;

    public String email;
    public String other;


    @Override
    public UserQuery apply(Consumer<UserQuery> consumer) {
        consumer.accept(this);
        return this;
    }

}
