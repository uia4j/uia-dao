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
public class SQLServerEnv implements Env {

    private String sqlserverConn;

    private String sqlserverUser;

    private String sqlserverPwd;

    private String sqlserverSchema;

    static {
        try {
        	DriverManager.registerDriver(new com.microsoft.sqlserver.jdbc.SQLServerDriver());
        }
        catch (Exception ex) {

        }
    }

    public SQLServerEnv() {
        this.sqlserverConn = "jdbc:sqlserver://localhost:1433;databaseName=unknown;schema=dbo";
        this.sqlserverUser = "admin";
        this.sqlserverPwd = "admin";
        this.sqlserverSchema = "dbo";
    }

    public SQLServerEnv(String conn, String user, String pwd, String schema) {
    	if(conn == null) {
            this.sqlserverConn = "jdbc:sqlserver://localhost:1433;databaseName=unknown;schema=dbo";
            this.sqlserverUser = "admin";
            this.sqlserverPwd = "admin";
            this.sqlserverSchema = "dbo";
    	}
    	else {
    		this.sqlserverConn = conn;
    		this.sqlserverUser = user;
    		this.sqlserverPwd = pwd;
    		this.sqlserverSchema = schema;
    	}
    }

    @Override
    public String test() {
        return String.format("%s, user:%s, schema:%s", this.sqlserverConn, this.sqlserverUser, this.sqlserverSchema);
    }

    @Override
    public Connection create() throws SQLException {
        Connection conn = java.sql.DriverManager.getConnection(this.sqlserverConn, this.sqlserverUser, this.sqlserverPwd);
        conn.setSchema(this.sqlserverSchema);
        return conn;
    }
}