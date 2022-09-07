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
public class HanaEnv implements Env {

    private String hanaConn;

    private String hanaUser;

    private String hanaPwd;

    private String hanaSchema;

    static {
        try {
            DriverManager.registerDriver(new com.sap.db.jdbc.Driver());
        }
        catch (Exception e) {

        }
    }

    public HanaEnv() {
        this.hanaConn = "jdbc:sap://localhost:39015?connectTimeout=5000&communicationTimeout=5000";
        this.hanaUser = "SYS";
        this.hanaPwd = "SYS";
    }

    public HanaEnv(String conn, String user, String pwd, String schema) {
        if (conn == null) {
            this.hanaConn = "jdbc:sap://localhost:39015?connectTimeout=5000&communicationTimeout=5000";
            this.hanaUser = "SYS";
            this.hanaPwd = "SYS";
        }
        else {
            this.hanaConn = conn.contains("?") ? conn : conn + "?connectTimeout=5000&communicationTimeout=5000";
            this.hanaUser = user;
            this.hanaPwd = pwd;
            this.hanaSchema = schema;
        }
    }

    @Override
    public String test() {
        return String.format("%s, user:%s, schema:%s", this.hanaConn, this.hanaUser, this.hanaSchema);
    }

    @Override
    public Connection create() throws SQLException {
        return java.sql.DriverManager.getConnection(this.hanaConn, this.hanaUser, this.hanaPwd);
    }
}