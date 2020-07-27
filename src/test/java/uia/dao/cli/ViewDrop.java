package uia.dao.cli;

import java.util.List;

import org.junit.Test;

import uia.dao.Database;

public class ViewDrop extends AbstractCmd {

    @Test
    public void test() throws Exception {
        execute(new String[] {
                "-s", "pgsvr96",
                "-t", "pgsvr96",
                "--view", null });
    }

    public void execute(String[] args) throws Exception {
        Database source = byReal(args[1]);
        Database target = byReal(args[3]);

        List<String> vns = source.selectViewNames();
        List<String> _vns = target.selectViewNames();
        for (String vn : vns) {
            if (_vns.contains(vn.toUpperCase()) || _vns.contains(vn.toLowerCase())) {
                target.dropView(vn);
            }
        }
    }

}
