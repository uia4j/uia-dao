package uia.dao.cli;

import java.util.List;

import org.junit.Test;

import uia.dao.Database;

public class ViewNamePrint extends AbstractCmd {

    @Test
    public void test() throws Exception {
        execute(new String[] {
                "-s", "aliyun" });
    }

    public void execute(String[] args) throws Exception {
        Database source = byReal(args[1]);

        // view
        List<String> vns = source.selectViewNames();
        System.out.println("\n-- views:" + vns.size());
        for (String vn : vns) {
            System.out.println(vn);
        }
    }

}
