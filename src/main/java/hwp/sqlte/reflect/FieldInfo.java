package hwp.sqlte.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class FieldInfo {

    private Method setter;
    private Method getter;
    private Field field;
    private boolean _public;

    public FieldInfo(Field field, boolean isPublic) {
        this.field = field;
        this._public = isPublic;
    }

    public FieldInfo(Field field, Method setter) {
        this.field = field;
        this.setter = setter;
    }

    public void setValue(Object obj, Object value) throws ReflectiveOperationException {
        if (_public) {
            this.field.set(obj, value);
        } else if (setter != null) {
            setter.invoke(obj, value);
        } else {

        }
    }

    public Object getValue(Object obj) throws ReflectiveOperationException {
        if (_public) {
            return field.get(obj);
        }
        return getter.invoke(obj);
    }

}