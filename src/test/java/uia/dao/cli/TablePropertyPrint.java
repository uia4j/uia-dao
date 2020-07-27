package uia.dao.cli;

import org.junit.Test;

import uia.dao.Database;

public class TablePropertyPrint extends AbstractCmd {

    @Test
    public void test() throws Exception {
        execute(new String[] {
                "-s", "pgsvr96",
                "--table", null });
    }

    public void execute(String[] args) throws Exception {
        Database source = byReal(args[1]);
        for(String tn : source.selectTableNames() ){
        	System.out.println(source.selectTable(tn, false).generateProperties());
        }
    }

}
