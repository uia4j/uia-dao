package uia.presto;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.junit.Test;

import uia.dao.PrestoDao;
import uia.dao.annotation.LakeColumnInfo;
import uia.dao.annotation.LakeSelectInfo;
import uia.dao.where.Where;

public class PrestoDaoTest {

    @Test
    public void test() throws Exception {
        Properties sysPrpps = System.getProperties();
        sysPrpps.setProperty("javax.net.ssl.trustStore", "D:/temp/prestodb_cert.jks");
        sysPrpps.setProperty("javax.net.ssl.trustStorePassword", "Pdb12345");
        System.setProperties(sysPrpps);

        Properties jdbcProps = new Properties();
        jdbcProps.setProperty("user", "kyle");
        jdbcProps.setProperty("password", "K@nlin5953");
        jdbcProps.setProperty("SSL", "true");
        Connection conn = DriverManager.getConnection("jdbc:presto://coordinator1.prestodb.htks.com:19287", jdbcProps);
        Statement stat = conn.createStatement();
        ResultSet rs = stat.executeQuery("SELECT s.sfc,t.report_id " +
                "FROM trek.trek.report_info AS t " +
                "INNER JOIN mes.wip.sfc AS s ON s.handle=t.fab_lot_name " +
                "WHERE s.handle='SFCBO:1020,WDA7M219.1'");
        while (rs.next()) {
            System.out.println(rs.getString(1) + rs.getString(2));
        }
    }

    @Test
    public void testORM1() throws Exception {
        try (PrestoDao dao = new PrestoDao("D:/temp/prestodb_cert.jks", "Pdb12345")) {
            dao.connect(
                    "jdbc:presto://coordinator1.prestodb.htks.com:19287",
                    "kyle",
                    "K@nlin5953");

            List<Sample1> samples = dao.select(
                    Sample1.class,
                    Where.simpleAnd().eq("s.handle", "SFCBO:1020,WDA7M219.1"),
                    "t.report_id");
            for (Sample1 sample : samples) {
                System.out.printf("%s %s %tF %tT\n",
                        sample.getSfcName(),
                        sample.getReportId(),
                        sample.getUploadTime(),
                        sample.getUploadTime());
            }
        }
    }

    @Test
    public void testORM2() throws Exception {
        try (PrestoDao dao = new PrestoDao("D:/temp/prestodb_cert.jks", "Pdb12345")) {
            dao.connect(
                    "jdbc:presto://coordinator1.prestodb.htks.com:19287",
                    "kyle",
                    "K@nlin5953");

            long t1 = System.currentTimeMillis();
            List<Sample2> samples = dao.select(
                    Sample2.class,
                    Where.simpleAnd().eq("t.equip_id", "BCUE04").eq("t.track_run", 1720079561706238L), "t.track_run");
            for (Sample2 sample : samples) {
                System.out.printf("%s %s\n",
                        sample.getSfcName(),
                        sample.getItemQty());
            }
            long t2 = System.currentTimeMillis();
            System.out.println(t2 - t1);
        }
    }

    @LakeSelectInfo(sql = "SELECT s.sfc,t.report_id,t.upload_time " +
            "FROM trek.trek.report_info AS t " +
            "INNER JOIN mes.wip.sfc AS s ON s.handle=t.fab_lot_name ")
    public static class Sample1 {

        @LakeColumnInfo(index = 1)
        private String reportId;

        @LakeColumnInfo(index = 0)
        private String sfcName;

        @LakeColumnInfo(index = 2, toLocal = true)
        private Date uploadTime;

        public String getReportId() {
            return this.reportId;
        }

        public void setReportId(String reportId) {
            this.reportId = reportId;
        }

        public String getSfcName() {
            return this.sfcName;
        }

        public void setSfcName(String sfcName) {
            this.sfcName = sfcName;
        }

        public Date getUploadTime() {
            return this.uploadTime;
        }

        public void setUploadTime(Date uploadTime) {
            this.uploadTime = uploadTime;
        }
    }

    @LakeSelectInfo(sql = "SELECT t.lot_id,s.item_qty " +
            "FROM pie.public.track_log t " +
            "INNER JOIN mes.wip.view_sfc s ON s.sfc_name=t.lot_id ")
    public static class Sample2 {

        @LakeColumnInfo(index = 0)
        private String sfcName;

        @LakeColumnInfo(index = 1)
        private String itemQty;

        public String getSfcName() {
            return this.sfcName;
        }

        public void setSfcName(String sfcName) {
            this.sfcName = sfcName;
        }

        public String getItemQty() {
            return this.itemQty;
        }

        public void setItemQty(String itemQty) {
            this.itemQty = itemQty;
        }

    }
}
