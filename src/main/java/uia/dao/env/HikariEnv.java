package uia.dao.env;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class HikariEnv implements Env {

    private final HikariConfig config;

    private final HikariDataSource ds;

    public HikariEnv() {
        this("jdbc:postgresql://localhost:5432/postgres", "postgres", "postgres");
    }

    public HikariEnv(String conn, String user, String pwd) {
        this.config = new HikariConfig();
        this.config.setJdbcUrl(conn);
        this.config.setUsername(user);
        this.config.setPassword(pwd);
        this.config.setMaximumPoolSize(50);
        this.config.setConnectionTimeout(5000);
        this.config.setMinimumIdle(5);
        this.config.addDataSourceProperty("cachePrepStmts", "true");
        this.config.addDataSourceProperty("prepStmtCacheSize", "25");
        this.config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        this.ds = new HikariDataSource(this.config);
    }

    public HikariEnv(String conn, String user, String pwd, Properties dsProperties) {
        this.config = new HikariConfig();
        this.config.setJdbcUrl(conn);
        this.config.setUsername(user);
        this.config.setPassword(pwd);
        this.config.setMaximumPoolSize(50);
        this.config.setConnectionTimeout(5000);
        this.config.setMinimumIdle(5);
        this.config.setDataSourceProperties(dsProperties);

        this.ds = new HikariDataSource(this.config);
    }

    @Override
    public void close() {
        this.ds.close();
    }

    @Override
    public String test() {
        return String.format("%s, user:%s", this.config.getJdbcUrl(), this.config.getUsername());
    }

    @Override
    public Connection create() throws SQLException {
        return this.ds.getConnection();
    }
}
