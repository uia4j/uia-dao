package uia.dao.cli;

import org.junit.Test;

import uia.dao.Database;
import uia.dao.ScriptTool;

public class DBDiffScriptSave extends AbstractCmd {

    @Test
    public void test() throws Exception {
        execute(new String[] {
                "-s", "pgsvr96",
                "-c", "pgsvr96_v2",
                "--dbtype", "hana",
                "--file", "d:/temp/pmsdb/pmsdbv2_diff_hana.sql" });
    }

    public void execute(String[] args) throws Exception {
        Database source = byReal(args[1]);
        Database compare = byReal(args[3]);
        Database target = byType(args[5]);

        ScriptTool tool = new ScriptTool(source);
        tool.toAlterScript(args[7], compare, target);
    }

}
