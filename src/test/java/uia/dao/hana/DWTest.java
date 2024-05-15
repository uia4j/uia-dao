package uia.dao.hana;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import uia.dao.AbstractDatabase.IndexInfo;
import uia.dao.ComparePlan;
import uia.dao.CompareResult;
import uia.dao.Database;
import uia.dao.TableType;
import uia.dao.pg.PostgreSQL;

public class DWTest {

    @Test
    public void testConn() throws Exception {
        try (Hana h = erpProd()) {
            h.getConnection().close();
        }
        try (Hana h = erpDev()) {
            h.getConnection().close();
        }
    }

    @Test
    public void testGo() throws Exception {
        try (Hana source = wipTestKS()) {
            source.setAlwaysNVarchar(true);
            List<String> tables = source.selectTableNames();
            Collections.sort(tables, (a, b) -> a.compareTo(b));

            try (Hana target = wipTestJS()) {
                target.setAlwaysNVarchar(true);
                for (String tn : tables) {
                    if (target.exists(tn)) {
                        System.out.println(tn + " exists");
                        continue;
                    }

                    System.out.println(tn);
                    TableType ta = source.selectTable(tn, false);
                    String[] sqls = target.generateCreateTableSQL(ta).split(";");
                    for (String sql : sqls) {
                        if (sql == null || sql.trim().isEmpty()) {
                            continue;
                        }
                        System.out.println("  " + target.execute(sql));
                    }
                }
            }
        }
    }

    @Test
    public void testView() throws Exception {
        try (Hana source = wipTestKS()) {
            String sql = source.selectViewScript("V_DELAY_SCHEDULE");
            System.out.println(sql);
            try (Hana target = wipTestJS()) {
                target.createView("V_DELAY_SCHEDULE", sql);
            }
        }
    }

    public void testGo2() throws Exception {
        try (Hana source = wipTestKS()) {
            source.setAlwaysNVarchar(true);
            try (Hana target = wipTestJS()) {
                target.setAlwaysNVarchar(true);
                System.out.println(" -- ZR_FILE");
                source.copy("ZR_FILE", target, 5000);
            }
        }
    }

    @Test
    public void testDiff() throws Exception {
        ComparePlan cp1 = new ComparePlan(
                false,  // strict char
                true,   // strict numeric
                true,   // strict datetime
                false,   // check nullable
                false);  // check data size

        try (Hana source = wipTestKS()) {
            List<String> tns = source.selectTableNames();
            System.out.println("total=" + tns.size());
            source.setAlwaysNVarchar(true);
            try (Hana target = wipTestJS()) {
                target.setAlwaysNVarchar(true);
                for (final String tn : tns) {
                    System.out.println("--" + tn);
                    TableType ta = source.selectTable(tn, false);
                    TableType tb = target.selectTable(tn, false);
                    CompareResult cr = ta.sameAs(tb, cp1);
                    if (!cr.isPassed()) {
                        if (cr.isMissing()) {
                            String[] sqls = target.generateCreateTableSQL(ta).split(";");
                            for (String sql : sqls) {
                                if (!sql.trim().isEmpty()) {
                                    System.out.println(sql + ";");
                                }
                            }
                        }
                        else {
                            System.out.println("--" + cr.getMessages());
                            String[] sqls = target.generateAlterTableSQL(tn, cr.getDiff()).split(";");
                            for (String sql : sqls) {
                                if (!sql.trim().isEmpty()) {
                                    System.out.println(sql + ";");
                                }
                            }
                        }
                        return;
                    }
                }
            }
            catch (Exception ex) {
            }
        }

        /**
        try (Hana source = wip38KS()) {
            try (Hana target = wip38JS()) {
                target.setAlwaysNVarchar(true);
                ComparePlan cp2 = new ComparePlan(
                        false,
                        false,
                        false,
                        false,
                        false);
                List<String> vns = source.selectViewNames();
                for (String vn : vns) {
                    System.out.println("==" + vn);
        
                    TableType va = source.selectTable(vn, false);
                    TableType vb = target.selectTable(vn, false);
                    CompareResult cr = va.sameAs(vb, cp2);
                    if (!cr.isPassed()) {
                        System.out.println(target.generateDropViewSQL(vn) + ";");
                        System.out.println(target.generateCreateViewSQL(vn, source.selectViewScript(vn)) + ";");
                        System.out.println();
                    }
                }
            }
        }
         */
    }

    @Test
    public void testDiffIndex() throws Exception {
        try (Database source = new PostgreSQL("localhost", "5432", "dsimdb", "WIP", "WIP")) {
            try (Hana target = new Hana("10.160.2.38", "30015", "ROAD", "ROAD", "Road12345")) {
                //try (Hana target = new Hana("10.160.2.23", "31041", "DSIM", "DSIM", "Dsim12345")) {

                List<String> tns = source.selectTableNames();
                for (String tn : tns) {
                    Map<String, IndexInfo> ia = source.selectIndexScripts(tn);
                    Map<String, IndexInfo> ib = target.selectIndexScripts(tn);
                    for (String key : ia.keySet()) {
                        if (key.endsWith("PKEY")) {// PG issue
                            continue;
                        }

                        IndexInfo from = ia.get(key);
                        IndexInfo to = ib.remove(key);
                        if (to == null) {
                            System.out.println("\n--" + key + " missing");
                            System.out.println(from.script());
                        }
                        else if (!from.same(to)) {
                            System.out.println("\n--" + key + " not match");
                            System.out.println("DROP INDEX " + key + ";");
                            System.out.println(from.script());
                        }
                    }
                    for (String key : ib.keySet()) {
                        System.out.println("\n--" + key + " drop");
                        IndexInfo from = ib.get(key);
                        System.out.println("DROP INDEX " + key + ";");
                        System.out.println(from.script());
                    }
                }
            }
        }
    }

    private Hana erpProd() throws SQLException {
        return new Hana("10.170.110.50", "30015", "ERP_PROD", "ERP_PROD", "4#Kd8Bdm");
    }

    private Hana erpDev() throws SQLException {
        return new Hana("10.170.110.50", "30015", "ERP_DEV", "ERP_DEV", "WM#kFc8u");
    }

    private Hana wipTestKS() throws SQLException {
        return new Hana("10.160.2.23", "31041", "WIP", "WIP", "Sap12345", "connectTimeout=5000&communicationTimeout=120000");
    }

    private Hana wipTestJS() throws SQLException {
        return new Hana("10.160.2.23", "31015", "WIP", "WIP", "Sap12345js", "connectTimeout=5000&communicationTimeout=120000");
    }
}
