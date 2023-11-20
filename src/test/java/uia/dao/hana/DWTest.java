package uia.dao.hana;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
    public void testDiffOne() throws Exception {
        try (Hana source = erpDev()) {
            try (Hana target = erpProd()) {
                target.setAlwaysNVarchar(true);

                ComparePlan cp1 = new ComparePlan(
                        false,  // strict char
                        true,   // strict numeric
                        true,   // strict datetime
                        true,   // check nullable
                        true);  // check data size

                String tn = "DT__4062778_1922_1_1_16160";
                TableType ta = source.selectTable(tn, false);
                TableType tb = target.selectTable(tn, false);
                CompareResult cr = ta.sameAs(tb, cp1);

                System.out.println("--" + tn);
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
                }
            }
        }
    }

    @Test
    public void testDiff() throws Exception {
        List<String> tns = Collections.emptyList();
        try (Hana source = erpProd38()) {
            tns = source.selectTableNames();
            System.out.println("total=" + tns.size());
        }

        ComparePlan cp1 = new ComparePlan(
                false,  // strict char
                true,   // strict numeric
                true,   // strict datetime
                true,   // check nullable
                true);  // check data size

        ExecutorService es = Executors.newFixedThreadPool(4);
        for (final String tn : tns) {
            es.execute(new Runnable() {

                @Override
                public void run() {
                    try (Hana source = erpProd38()) {
                        try (Hana target = erpDev()) {
                            // target.setAlwaysNVarchar(true);
                            TableType ta = source.selectTable(tn, false);
                            TableType tb = target.selectTable(tn, false);
                            CompareResult cr = ta.sameAs(tb, cp1);
                            if (!cr.isPassed()) {
                                System.out.println("--" + tn);
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
                            }
                        }
                    }
                    catch (Exception ex) {
                    }
                }
            });
        }
        es.shutdown();
        es.awaitTermination(10, TimeUnit.MINUTES);
        es.shutdownNow();

        try (Hana source = erpDev()) {
            try (Hana target = erpProd()) {
                target.setAlwaysNVarchar(true);
                ComparePlan cp2 = new ComparePlan(
                        false,
                        false,
                        false,
                        false,
                        false);
                List<String> vns = source.selectViewNames();
                for (String vn : vns) {
                    TableType va = source.selectTable(vn, false);
                    TableType vb = target.selectTable(vn, false);
                    CompareResult cr = va.sameAs(vb, cp2);
                    if (!cr.isPassed()) {
                        System.out.println("--" + vn);
                        System.out.println(target.generateDropViewSQL(vn) + ";");
                        System.out.println(target.generateCreateViewSQL(vn, source.selectViewScript(vn)) + ";");
                        System.out.println();
                    }
                }
            }
        }

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

    private Hana hrProd() throws SQLException {
        return new Hana("10.170.110.50", "30015", "HR_PROD", "HR_PROD", "h7Vut&WK");
    }

    private Hana erpProd38() throws SQLException {
        return new Hana("10.160.2.38", "30015", "ERP_PROD", "ERP_PROD", "Sap12345prod");
    }

}
