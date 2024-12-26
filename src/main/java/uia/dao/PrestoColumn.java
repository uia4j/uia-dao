package uia.dao;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.TimeZone;

import uia.dao.annotation.LakeColumnInfo;

public class PrestoColumn {

    final Field field;

    final LakeColumnInfo ci;

    PrestoColumn(Field f, LakeColumnInfo ci) {
        this.field = f;
        this.ci = ci;
    }

    void apply(Object obj, Object value) throws Exception {
        if (value != null && value instanceof Date && this.ci.toLocal()) {
            this.field.set(obj, new Date(((Date) value).getTime() + TimeZone.getDefault().getRawOffset()));
        }
        else {
            this.field.set(obj, value);
        }
    }

    String typeName() {
        return this.field.getType().getSimpleName();
    }

}
