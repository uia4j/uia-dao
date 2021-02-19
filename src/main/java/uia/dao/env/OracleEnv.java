package uia.dao.env;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * HANA helper.
 *
 * @author Kyle K. Lin
 *
 */
public class OracleEnv implements Env {

    /**
     * Connection string.
     */
    private String oraConn;

    /**
     * User id.
     */
    private String oraUser;

    /**
     * Password.
     */
    private String oraPwd;

    /**
     * Schema
     */
    private String oraSchema;

    static {
        try {
        	DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
        }
        catch (Exception ex) {
        	
        }
    }

    public OracleEnv() {
        this.oraConn = "jdbc:oracle:thin:@//localhost:1521/orcl.localdomain";
        this.oraUser = "orcl";
        this.oraPwd = "orcl";
        this.oraSchema = null;
    }

    public OracleEnv(String conn, String user, String pwd, String schema) {
    	if(conn == null) {
            this.oraConn = "jdbc:oracle:thin:@//localhost:1521/orcl.localdomain";
            this.oraUser = "orcl";
            this.oraPwd = "orcl";
            this.oraSchema = null;
    	}
    	else {
            this.oraConn = conn;
            this.oraUser = user;
            this.oraPwd = pwd;
            this.oraSchema = schema;
    	}
    }

    @Override
    public String test() {
        return String.format("%s, user:%s, schema:%s", oraConn, oraUser, oraSchema);
    }

    @Override
    public Connection create() throws SQLException {
        return java.sql.DriverManager.getConnection(oraConn, oraUser, oraPwd);
    }
}