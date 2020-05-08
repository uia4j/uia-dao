package uia.dao.cli;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

import uia.dao.Database;

public class FileExecute extends AbstractCmd {

    @Test
    public void test() throws Exception {
        execute(new String[] {
                "-s", "wip",
                "--mode", "folder",
                "--file", "d:/temp/wip" });
    }

    public void execute(String[] args) throws Exception {
        Database source = byReal(args[1]);

        File file = new File(args[5]);
        String[] sqlFiles = file.list((dir, name) -> name.toLowerCase().endsWith(".sql"));
        for(String sqlFile : sqlFiles) {
        	System.out.println(sqlFile);
        	String sql = String.join(
        			"\n", 
        			Files.readAllLines(Paths.get(file.getAbsolutePath() + "/" + sqlFile)));
        	
        	source.execute(sql);
        }
    }

}
