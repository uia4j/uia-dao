package uia.dao.cli;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import uia.dao.Database;

public class DBCreateScriptPrint extends AbstractCmd {

    @Test
    public void test() throws Exception {
        execute(new String[] {
                "-s", "pgsvr96",
                "--dbtype", "ora",
                "--table", null,
                "--view", null });
    }

    public void execute(String[] args) throws Exception {
        Database source = byReal(args[1]);
        Database target = byType(args[3]);

        // table
        List<String> tns = args[5] != null ? Arrays.asList(args[5].split(",")) : source.selectTableNames();
        for (String tn : tns) {
            System.out.println(target.generateCreateTableSQL(source.selectTable(tn, false)));
        }

        // view
        List<String> vns = args[7] != null ? Arrays.asList(args[7].split(",")) : source.selectViewNames();
        for (String vn : vns) {
            System.out.print(target.generateCreateViewSQL(vn, source.selectViewScript(vn)));
            System.out.println(";\n");
        }
    }

}
