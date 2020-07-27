package uia.dao.cli;

import java.util.List;

import org.junit.Test;

import uia.dao.Database;

public class DBDropScriptPrint extends AbstractCmd {

    @Test
    public void test() throws Exception {
        execute(new String[] {
                "-s", "pgsvr96",
                "-t", "pgsvr96",
                "--dbtype", "ora",
                "--table", null,
                "--view", null });
    }

    public void execute(String[] args) throws Exception {
        Database source = byReal(args[1]);
        Database compare = byReal(args[3]);
        Database target = byType("hana");

        // view
        List<String> vns = source.selectViewNames();
        List<String> _vns = compare.selectViewNames();
        for (String vn : vns) {
            if (_vns.contains(vn.toUpperCase()) || _vns.contains(vn.toLowerCase())) {
                System.out.println(target.generateDropViewSQL(vn) + ";");
            }
        }

        // table
        List<String> tns = source.selectTableNames();
        List<String> _tns = compare.selectTableNames();
        for (String tn : tns) {
            if (_tns.contains(tn.toUpperCase()) || _tns.contains(tn.toLowerCase())) {
                System.out.println(target.generateDropTableSQL(tn) + ";");
            }
        }

    }

}
