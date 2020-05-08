package uia.dao.cli;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import uia.dao.ComparePlan;
import uia.dao.CompareResult;
import uia.dao.Database;
import uia.dao.TableType;

public class DBDiffScriptPrint extends AbstractCmd {

    @Test
    public void test() throws Exception {
        execute(new String[] {
                "-s", "pgsvr96",
                "-t", "aliyun",
                "--table", null,
                "--view", null,
                "--dbtype", "hana" });
    }

    public void execute(String[] args) throws Exception {
        Database source = byReal(args[1]);
        Database target = byReal(args[3]);
        Database dbtype = byType(args[9]);

        // table
        List<String> tns = args[5] != null ? Arrays.asList(args[5].split(",")) : source.selectTableNames();
        for (String tn : tns) {
            TableType ta = source.selectTable(tn, false);
            TableType tb = target.selectTable(tn, false);
            CompareResult cr = ta.sameAs(tb, new ComparePlan(false, true, true, true, true));

            System.out.println("-- " + tn);
            if (!cr.isPassed()) {
                if (cr.isMissing()) {
                    System.out.println(dbtype.generateCreateTableSQL(ta));
                }
                else {
                    System.out.println(dbtype.generateAlterTableSQL(tn, cr.getDiff()));
                }
            }
        }

        // view
        List<String> vns = args[7] != null ? Arrays.asList(args[7].split(",")) : source.selectViewNames();
        for (String vn : vns) {
            TableType va = source.selectTable(vn, false);
            TableType vb = target.selectTable(vn, false);
            CompareResult cr = va.sameAs(vb, ComparePlan.view());

            System.out.println("-- " + vn);
            if (!cr.isPassed()) {
                if (!cr.isMissing()) {
                    System.out.println(dbtype.generateDropViewSQL(vn) + ";");
                }
                System.out.println(dbtype.generateCreateViewSQL(vn, source.selectViewScript(vn)) + ";");
                System.out.println();
            }
        }
    }
}
