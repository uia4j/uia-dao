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
public class MySQLEnv implements Env {

    /**
     * Connection string.
     */
    private String myConn;

    /**
     * User id.
     */
    private String myUser;

    /**
     * Password.
     */
    private String myPwd;

    /**
     * Schema.
     */
    private String mySchema;

    static {
        try {
            DriverManager.registerDriver(new com.mysql.jdbc.Driver());
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public MySQLEnv() {
        this.myConn = "jdbc:mysql://localhost:3066";
        this.myUser = "admin";
        this.myPwd = "admin";
        this.mySchema = "sys";
    }

    public MySQLEnv(String conn, String user, String pwd, String schema) {
        if (conn == null) {
            this.myConn = "jdbc:mysql://localhost:3066";
            this.myUser = "admin";
            this.myPwd = "admin";
            this.mySchema = "sys";
        }
        else {
            this.myConn = conn;
            this.myUser = user;
            this.myPwd = pwd;
            this.mySchema = schema;
        }
    }

    @Override
    public String test() {
        return String.format("%s, user:%s, schema:%s", this.myConn, this.myUser, this.mySchema);
    }

    @Override
    public Connection create() throws SQLException {
        Connection conn = DriverManager.getConnection(this.myConn, this.myUser, this.myPwd);
        conn.setSchema(this.mySchema);
        return conn;
    }
}
