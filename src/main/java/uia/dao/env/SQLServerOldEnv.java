package uia.dao.env;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * HANA helper.
 *
 * @author Kyle K. Lin
 *
 */
public class SQLServerOldEnv implements Env {

    private String sqlserverConn;

    private String sqlserverUser;

    private String sqlserverPwd;

    private String sqlserverSchema;

    static {
    }

    public SQLServerOldEnv() {
        this.sqlserverConn = "jdbc:jtds:sqlserver://localhost:1433/unknown";
        this.sqlserverUser = "admin";
        this.sqlserverPwd = "admin";
        this.sqlserverSchema = "dbo";
    }

    public SQLServerOldEnv(String conn, String user, String pwd, String schema) {
        if (conn == null) {
            this.sqlserverConn = "jdbc:jtds:sqlserver://localhost:1433/unknown";
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
        return conn;
    }
}