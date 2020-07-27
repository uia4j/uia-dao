package uia.dao.cli;

import org.junit.Test;

import uia.dao.Database;
import uia.dao.ScriptTool;

public class ViewCreateScriptSave extends AbstractCmd {

    @Test
    public void test() throws Exception {
        String dbtype = "ora";
        execute(new String[] {
                "-s", "pgsvr96",
                "--dbtype", dbtype,
                "--file", "d:/temp/pmsdb/200505/pmsdbv2_" + dbtype + "_view.sql",
                "--view", null });
    }

    public void execute(String[] args) throws Exception {
        Database source = byReal(args[1]);
        Database target = byType(args[3]);

        ScriptTool tool = new ScriptTool(source);
        // view
        tool.toViewScript(args[5], target, args[7] != null ? args[7].split(",") : new String[0]);
    }

}
