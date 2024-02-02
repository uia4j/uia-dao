package uia.dao.env;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

public class DataSourceEnv implements Env {
	
    private final DataSource ds;

    public DataSourceEnv(DataSource ds) {
    	this.ds = ds;
    }

    @Override
    public void close() {
    }

    @Override
    public String test() {
        return "external ds";
    }

    @Override
    public Connection create() throws SQLException {
        return this.ds.getConnection();
    }
}
