package uia.dao.env;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * HANA helper.
 *
 * @author Kyle K. Lin
 *
 */
public class AppSourceEnv implements Env {

    /**
     * Connection string.
     */
    private String sourceName;

    public AppSourceEnv() {
		this.sourceName = "jdbc/jts/daoPool";
    }

    public AppSourceEnv(String sourceName) {
    	if(sourceName == null) {
        	this.sourceName = "jdbc/jts/daoPool";
    	}
    	else {
    		this.sourceName = sourceName;
    	}
    }

    public String test() {
        return "dataSource:" + sourceName;
    }

    public Connection create() throws SQLException {
        try {
            Context ctx = new InitialContext();
            DataSource dataSource = (DataSource) ctx.lookup(this.sourceName);

            return dataSource.getConnection();
        }
        catch (NamingException ex) {
            throw new SQLException("DataSource failed", ex);
        }
    }
}