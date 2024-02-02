package uia.dao.env;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;

public class DruidEnv implements Env {
	
	private final String conn;
	
	private final String user;
	
    private final DruidDataSource ds;

    public DruidEnv() throws Exception {
        this("jdbc:postgresql://localhost:5432/postgres", "postgres", "postgres");
    }

    public DruidEnv(String conn, String user, String pwd) {
    	Map<String, String> config = new HashMap<>();
    	config.put(DruidDataSourceFactory.PROP_URL, conn);
    	config.put(DruidDataSourceFactory.PROP_DRIVERCLASSNAME, "com.sap.db.jdbc.Driver");
    	config.put(DruidDataSourceFactory.PROP_USERNAME, user);
    	config.put(DruidDataSourceFactory.PROP_PASSWORD, pwd);
    	config.put(DruidDataSourceFactory.PROP_INITIALSIZE, "10");
    	config.put(DruidDataSourceFactory.PROP_MAXACTIVE, "100");
    	config.put(DruidDataSourceFactory.PROP_MAXWAIT, "5000");
    	config.put(DruidDataSourceFactory.PROP_LOGABANDONED, "true");
    	config.put(DruidDataSourceFactory.PROP_REMOVEABANDONED, "true");
    	config.put(DruidDataSourceFactory.PROP_REMOVEABANDONEDTIMEOUT, "3600");
    	config.put(DruidDataSourceFactory.PROP_TIMEBETWEENEVICTIONRUNSMILLIS, "60000");
    	config.put(DruidDataSourceFactory.PROP_MINEVICTABLEIDLETIMEMILLIS, "300000");
    	config.put(DruidDataSourceFactory.PROP_VALIDATIONQUERY, "SELECT 1 FROM dummy");
    	config.put(DruidDataSourceFactory.PROP_TESTWHILEIDLE, "true");
    	config.put(DruidDataSourceFactory.PROP_TESTONBORROW, "false");
    	config.put(DruidDataSourceFactory.PROP_TESTONRETURN, "false");
    	config.put(DruidDataSourceFactory.PROP_POOLPREPAREDSTATEMENTS, "true");
    	config.put(DruidDataSourceFactory.PROP_MAXOPENPREPAREDSTATEMENTS, "50");
    	
    	this.conn = conn;
    	this.user = user;
        try {
			this.ds = (DruidDataSource) DruidDataSourceFactory.createDataSource(config);
		} catch (Exception e) {
			throw new Error(e.getMessage());
		}
    }

    @Override
    public void close() {
        this.ds.close();
    }

    @Override
    public String test() {
        return String.format("%s, user:%s", this.conn, this.user);
    }

    @Override
    public Connection create() throws SQLException {
        return this.ds.getConnection();
    }
}
