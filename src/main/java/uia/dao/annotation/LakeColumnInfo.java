package uia.dao.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface LakeColumnInfo {

    /**
     * Returns the column index in SQL statement.
     *
     * @return The column index from zero.
     */
    int index();

    /**
     * Changes the value to local time. This will be applied to java.util.Date type only.
     *
     * @return Convert to local time or not if the result is a Date object.
     */
    boolean toLocal() default false;
}
