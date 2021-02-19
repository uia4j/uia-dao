package uia.dao.env;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * PostgreSQL helper.
 *
 * @author Kyle K. Lin
 *
 */
public class PostgreSQLEnv implements Env {

    /**
     * Connection string.
     */
    private String pgConn;

    /**
     * User id.
     */
    private String pgUser;

    /**
     * Password.
     */
    private String pgPwd;

    /**
     * Schema.
     */
    private String pgSchema;

    static {
        try {
        	DriverManager.registerDriver(new org.postgresql.Driver());
        }
        catch (Exception ex) {

        }
    }
    
    public PostgreSQLEnv() {
		this.pgConn = "jdbc:postgresql://localhost:5432/postgres";
		this.pgUser = "postgres";
		this.pgPwd = "postgres";
		this.pgSchema = "public";
    }

    public PostgreSQLEnv(String conn, String user, String pwd, String schema) {
    	if(conn == null) {
    		this.pgConn = "jdbc:postgresql://localhost:5432/postgres";
    		this.pgUser = "postgres";
    		this.pgPwd = "postgres";
    		this.pgSchema = "public";
    	}
    	else {
    		this.pgConn = conn;
    		this.pgUser = user;
    		this.pgPwd = pwd;
    		this.pgSchema = schema;
    	}
    }

    @Override
    public String test() {
        return String.format("%s, user:%s, schema:%s", this.pgConn, this.pgUser, this.pgSchema);
    }

    @Override
    public Connection create() throws SQLException {
        Connection conn = DriverManager.getConnection(this.pgConn, this.pgUser, this.pgPwd);
        conn.setSchema(this.pgSchema);
        return conn;
    }
}
