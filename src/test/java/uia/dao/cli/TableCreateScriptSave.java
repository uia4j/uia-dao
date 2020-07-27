package uia.dao.cli;

import org.junit.Test;

import uia.dao.Database;
import uia.dao.ScriptTool;

public class TableCreateScriptSave extends AbstractCmd {

    @Test
    public void test() throws Exception {
        String dbtype = "pg";
        execute(new String[] {
                "-s", "pgsvr96",
                "--dbtype", dbtype,
                "--mode", "all",
                "--file", "d:/temp/pmsdb/200505/pmsdbv2_" + dbtype + "_table.sql",
                "--table", null });
    }

    public void execute(String[] args) throws Exception {
        Database source = byReal(args[1]);
        Database target = byType(args[3]);

        String[] tables = null;
        if (args[9] == null) {
            tables = source.selectTableNames().toArray(new String[0]);
        }
        else {
            tables = args[9].split(",");
        }

        ScriptTool tool = new ScriptTool(source);
        if ("all".equalsIgnoreCase(args[5])) {
            tool.toTableScript(args[7], target, tables);
        }
        else {
            for (String table : tables) {
                String path = args[7] + table + ".sql";
                System.out.println(table + ": " + path);
                tool.toTableScript(path, target, table);
            }
        }
    }
}
