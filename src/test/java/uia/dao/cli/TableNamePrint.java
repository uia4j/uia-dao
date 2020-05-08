package uia.dao.cli;

import java.util.List;

import org.junit.Test;

import uia.dao.Database;

public class TableNamePrint extends AbstractCmd {

    @Test
    public void test() throws Exception {
        execute(new String[] {
                "-s", "aliyun_wip" });
    }

    public void execute(String[] args) throws Exception {
        Database source = byReal(args[1]);

        // table
        List<String> tns = source.selectTableNames();
        System.out.println("-- tables:" + tns.size());
        for (String tn : tns) {
            System.out.println(tn);
        }
    }

}
