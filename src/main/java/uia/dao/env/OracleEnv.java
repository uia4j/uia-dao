package uia.dao.env;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import oracle.jdbc.driver.OracleConnection;

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
            ex.printStackTrace();
        }
    }

    public OracleEnv() {
        this.oraConn = "jdbc:oracle:thin:@localhost:1521/orcl.localdomain";
        this.oraUser = "orcl";
        this.oraPwd = "orcl";
        this.oraSchema = null;
    }

    public OracleEnv(String conn, String user, String pwd, String schema) {
        if (conn == null) {
            this.oraConn = "jdbc:oracle:thin:@localhost:1521/orcl.localdomain";
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
        return String.format("%s, user:%s, schema:%s", this.oraConn, this.oraUser, this.oraSchema);
    }

    @Override
    public Connection create() throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", this.oraUser);
        props.setProperty("password", this.oraPwd);
        props.setProperty(OracleConnection.CONNECTION_PROPERTY_THIN_NET_CONNECT_TIMEOUT, "5000");
        props.setProperty(OracleConnection.CONNECTION_PROPERTY_THIN_READ_TIMEOUT, "30000");
        return java.sql.DriverManager.getConnection(this.oraConn, props);
    }
}