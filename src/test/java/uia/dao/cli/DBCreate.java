package uia.dao.cli;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import uia.dao.Database;

public class DBCreate extends AbstractCmd {

    @Test
    public void test() throws Exception {
        execute(new String[] {
                "-s", "pgsvr96",
                "-t", "kao_pms",
                "--table", null,
                "--view", null });
    }

    public void execute(String[] args) throws Exception {
        Database source = byReal(args[1]);
        Database target = byReal(args[3]);

        // table
        List<String> tns = args[5] != null ? Arrays.asList(args[5].split(",")) : source.selectTableNames();
        for (String tn : tns) {
            target.createTable(source.selectTable(tn, false));
        }

        // tool
        List<String> vns = args[7] != null ? Arrays.asList(args[7].split(",")) : source.selectViewNames();
        for (String vn : vns) {
            target.createView(vn, source.selectViewScript(vn));
        }
    }

}
