package uia.dao.cli;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import uia.dao.ComparePlan;
import uia.dao.CompareResult;
import uia.dao.Database;
import uia.dao.TableType;

public class DBDiff extends AbstractCmd {

    @Test
    public void test() throws Exception {
        execute(new String[] {
                "-s", "pgsvr96",
                "-t", "pgsvr96_0713",
                "--table", null,
                "--view", null });
    }

    public void execute(String[] args) throws Exception {
        Database source = byReal(args[1]);
        Database target = byReal(args[3]);

        // table
        List<String> tns = args[5] != null ? Arrays.asList(args[5].split(",")) : source.selectTableNames();
        for (String tn : tns) {
            TableType ta = source.selectTable(tn, false);
            TableType tb = target.selectTable(tn, false);
            CompareResult cr = ta.sameAs(tb, new ComparePlan(false, true, true, true, true));

            if (!cr.isPassed()) {
                System.out.println("-- " + tn);
                if (cr.isMissing()) {
                    target.createTable(ta);
                }
                else {
                    String[] sqls = target.generateAlterTableSQL(tn, cr.getDiff()).split(";");
                    target.executeBatch(Arrays.asList(sqls));
                }
            }
        }
    }

}
