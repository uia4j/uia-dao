package uia.dao.pms;

import uia.dao.annotation.ColumnInfo;
import uia.dao.annotation.TableInfo;

/**
 * Table or View: lookup.
 *
 * @author UIA
 */
@TableInfo(name = "lookup", orderBy = "id,sub_id,param_name")
public class Lookup {

    public static final String KEY = "lookup";

    @ColumnInfo(name = "id", primaryKey = true)
    private String id;

    @ColumnInfo(name = "sub_id", primaryKey = true)
    private String subId;

    @ColumnInfo(name = "param_name", primaryKey = true)
    private String paramName;

    @ColumnInfo(name = "param_value")
    private String paramValue;

    /**
     * Constructor.
     *
     */
    public Lookup() {
    }

    /**
     * Constructor.
     *
     * @param data A instance to be cloned.
     */
    public Lookup(Lookup data) {
        this.id = data.id;
        this.subId = data.subId;
        this.paramName = data.paramName;
        this.paramValue = data.paramValue;

    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSubId() {
        return this.subId;
    }

    public void setSubId(String subId) {
        this.subId = subId;
    }

    public String getParamName() {
        return this.paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public String getParamValue() {
        return this.paramValue;
    }

    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }

    /**
     * Copies this instance.
     *
     * @return A new instance.
     */
    public Lookup copy() {
        return new Lookup(this);
    }

    @Override
    public String toString() {
        return this.id + ", " + this.subId + ", " + this.paramName;
    }
}
