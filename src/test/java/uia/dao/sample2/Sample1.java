package uia.dao.sample2;

import java.math.BigDecimal;
import java.util.Date;

import uia.dao.ColumnType.DataType;
import uia.dao.annotation.ColumnInfo;
import uia.dao.annotation.TableInfo;

@TableInfo(name = "sample_table", remark = "Sample")
public class Sample1 {

    @ColumnInfo(name = "key1", primaryKey = true, length = 64, remark = "key1")
    private String key1;

    @ColumnInfo(name = "key2", primaryKey = true, sqlType = DataType.INTEGER)
    private int key2;

    @ColumnInfo(name = "field_string", length = 100, remark = "fieldString")
    private String fieldString;

    @ColumnInfo(name = "field_short")
    private short fieldInt;

    @ColumnInfo(name = "field_long")
    private short fieldLong;

    @ColumnInfo(name = "field_int_string", sqlType = DataType.NVARCHAR)
    private short fieldIntString;

    @ColumnInfo(name = "field_numeric", length = 20, scale = 6)
    private BigDecimal fieldNumeric;

    @ColumnInfo(name = "field_bytes")
    private byte[] fieldBytes;

    @ColumnInfo(name = "field_timestamp_zone")
    private Date fieldTimez;

    @ColumnInfo(name = "field_timestamp", sqlType = DataType.TIMESTAMP)
    private Date fieldTime;
}
