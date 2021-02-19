package uia.dao.sample1;

import java.math.BigDecimal;
import java.sql.Clob;
import java.util.Date;

import uia.dao.annotation.ColumnInfo;
import uia.dao.annotation.TableInfo;

/**
 * Table or View: uia_dto_test.
 * 
 * @author UIA
 */
@TableInfo(name = "uia_dto_test")
public class Sample2 {
    
    public static final String KEY = "uia_dto_test";

    @ColumnInfo(name = "c1", primaryKey = true)
    private String c1;

    @ColumnInfo(name = "c2")
    private Clob c2;

    @ColumnInfo(name = "c3")
    private BigDecimal c3;

    @ColumnInfo(name = "c4")
    private Date c4;

    @ColumnInfo(name = "c5")
    private Date c5;

    /**
     * Constructor.
     *
     */
    public Sample2() {
    }	

    /**
     * Constructor.
     *
     * @param data A instance to be cloned.
     */
    public Sample2(Sample2 data) {
        this.c1 = data.c1;
        this.c2 = data.c2;
        this.c3 = data.c3;
        this.c4 = data.c4;
        this.c5 = data.c5;

    }	
    
    /**
     * Returns 主鍵.
     *
     * @return 主鍵.
     */
    public String getC1() {
        return this.c1;
    }

    /**
     * Sets 主鍵.
     *
     * @param c1 主鍵.
     */
    public void setC1(String c1) {
        this.c1 = c1;
    }

    /**
     * Returns 超長字串.
     *
     * @return 超長字串.
     */
    public Clob getC2() {
        return this.c2;
    }

    /**
     * Sets 超長字串.
     *
     * @param c2 超長字串.
     */
    public void setC2(Clob c2) {
        this.c2 = c2;
    }

    /**
     * Returns 數字.
     *
     * @return 數字.
     */
    public BigDecimal getC3() {
        return this.c3;
    }

    /**
     * Sets 數字.
     *
     * @param c3 數字.
     */
    public void setC3(BigDecimal c3) {
        this.c3 = c3;
    }

    /**
     * Returns 含時區時間.
     *
     * @return 含時區時間.
     */
    public Date getC4() {
        return this.c4 == null ? null : new Date(this.c4.getTime());
    }

    /**
     * Sets 含時區時間.
     *
     * @param c4 含時區時間.
     */
    public void setC4(Date c4) {
        this.c4 = c4 == null ? null : new Date(c4.getTime());
    }

    /**
     * Returns 不含時區時間.
     *
     * @return 不含時區時間.
     */
    public Date getC5() {
        return this.c5 == null ? null : new Date(this.c5.getTime());
    }

    /**
     * Sets 不含時區時間.
     *
     * @param c5 不含時區時間.
     */
    public void setC5(Date c5) {
        this.c5 = c5 == null ? null : new Date(c5.getTime());
    }

    /**
     * Copies this instance.
     *
     * @return A new instance.
     */
    public Sample2 copy() {
    	return new Sample2(this);
    }

    @Override
    public String toString() {
        return this.c1;
    }
}

