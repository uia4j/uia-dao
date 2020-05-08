package uia.dao.cli;

import java.util.List;

import org.junit.Test;

import uia.dao.Database;

public class DBDrop extends AbstractCmd {

    @Test
    public void test() throws Exception {
        execute(new String[] {
                "-s", "pgsvr96",
                "-t", "orasvr12",
                "--table", null,
                "--view", null });
    }

    public void execute(String[] args) throws Exception {
        Database source = byReal(args[1]);
        Database target = byReal(args[3]);

        // view
        List<String> vns = source.selectViewNames();
        List<String> _vns = target.selectViewNames();
        for (String vn : vns) {
            if (_vns.contains(vn.toUpperCase()) || _vns.contains(vn.toLowerCase())) {
                target.dropView(vn);
            }
        }

        // table
        List<String> tns = source.selectTableNames();
        List<String> _tns = target.selectTableNames();
        for (String tn : tns) {
            if (_tns.contains(tn.toUpperCase()) || _tns.contains(tn.toLowerCase())) {
                target.dropTable(tn);
            }
        }

    }

}
