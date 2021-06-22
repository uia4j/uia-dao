package uia.dao;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Executor;

import uia.dao.env.AppSourceEnv;
import uia.dao.env.Env;
import uia.dao.env.HanaEnv;
import uia.dao.env.HikariEnv;
import uia.dao.env.OracleEnv;
import uia.dao.env.PostgreSQLEnv;
import uia.dao.env.SQLServerEnv;

public abstract class DaoEnv {

    public static final String DATASOURCE = "DATASOURCE";

    public static final String DATAPOOL = "DATAPOOL";

    public static final String HANA = "HANA";

    public static final String ORACLE = "ORA";

    public static final String MSSQL = "MSSQL";

    public static final String POSTGRE = "PG";

    private final DaoFactory factory;

    private final String envName;

    private Env env;

    public static DaoEnv dataSource(final boolean dateToUTC, final String packageName) throws DaoException {
        return new DaoEnv(DATASOURCE, dateToUTC) {

            @Override
            protected void initialFactory(DaoFactory factory) throws Exception {
                factory.load(packageName);
            }
        };
    }

    public static DaoEnv pool(final boolean dateToUTC, final String packageName) throws DaoException {
        return new DaoEnv(DATAPOOL, dateToUTC) {

            @Override
            protected void initialFactory(DaoFactory factory) throws Exception {
                factory.load(packageName);
            }
        };
    }

    public static DaoEnv hana(final boolean dateToUTC, final String packageName) throws DaoException {
        return new DaoEnv(HANA, dateToUTC) {

            @Override
            protected void initialFactory(DaoFactory factory) throws Exception {
                factory.load(packageName);
            }
        };
    }

    public static DaoEnv oracle(final boolean dateToUTC, final String packageName) throws DaoException {
        return new DaoEnv(ORACLE, dateToUTC) {

            @Override
            protected void initialFactory(DaoFactory factory) throws Exception {
                factory.load(packageName);
            }
        };
    }

    public static DaoEnv postgre(final boolean dateToUTC, final String packageName) throws DaoException {
        return new DaoEnv(POSTGRE, dateToUTC) {

            @Override
            protected void initialFactory(DaoFactory factory) throws Exception {
                factory.load(packageName);
            }
        };
    }

    /**
     * The constructor.
     *
     * @param envName One of 'DATASOURCE','HANA','ORA','PG'.
     * @param dateToUTC Convert date with UTC time.
     * @throws DaoException Failed to initial factory.
     */
    public DaoEnv(String envName, boolean dateToUTC) throws DaoException {
        this.envName = envName;
        this.factory = new DaoFactory(dateToUTC);
        try {
            initialFactory(this.factory);
        }
        catch (Exception ex) {
            throw new DaoException(ex);
        }
    }

    public DaoFactory getDaoFactory() {
        return this.factory;
    }

    public Date toUTC(Date local) {
        return this.factory.toUTC(local);
    }

    public Date fromUTC(Date utc) {
        return this.factory.fromUTC(utc);
    }

    public void close() {
        this.env.close();
    }

    public <T> TableDaoHelper<T> forTable(Class<T> clz) {
        return this.factory == null
                ? null
                : this.factory.forTable(clz);
    }

    public <T> ViewDaoHelper<T> forView(Class<T> clz) {
        return this.factory == null
                ? null
                : this.factory.forView(clz);
    }

    public List<String> listTables() throws Exception {
        Set<String> tables = this.factory == null
                ? new TreeSet<>()
                : this.factory.getTables();
        ArrayList<String> result = new ArrayList<>();
        for (String t : tables) {
            result.add("" + Class.forName(t).getDeclaredField("KEY").get(null));
        }
        Collections.sort(result);
        return result;
    }

    public List<String> listViews() throws Exception {
        Set<String> views = this.factory == null
                ? new TreeSet<>()
                : this.factory.getViews();
        ArrayList<String> result = new ArrayList<>();
        for (String v : views) {
            result.add("" + Class.forName(v).getDeclaredField("KEY").get(null));
        }
        Collections.sort(result);
        return result;
    }

    public synchronized Map<String, String> test() throws SQLException {
        if (this.factory == null) {
            return new TreeMap<>();
        }
        try (Connection conn = create()) {
            return this.factory.test(conn);
        }
    }

    public synchronized String testConnString() {
        return this.env.test();
    }

    public synchronized DaoSession createSession() throws SQLException {
        return this.factory.createSession(create());
    }

    /**
     * Configure database.
     *
     * @param conn The JDBC connection string.
     * @param user The user id.
     * @param pwd The password.
     * @param schema The schema.
     * @return The instance.
     */
    public synchronized DaoEnv config(
            String conn,
            String user,
            String pwd,
            String schema) {
        if (DATASOURCE.equals(this.envName)) {
            this.env = new AppSourceEnv(conn);
        }
        if (DATAPOOL.equals(this.envName)) {
            this.env = new HikariEnv(conn, user, pwd);
        }
        else if (HANA.equals(this.envName)) {
            this.env = new HanaEnv(conn, user, pwd, schema);
        }
        else if (MSSQL.equals(this.envName)) {
            this.env = new SQLServerEnv(conn, user, pwd, schema);
        }
        else if (ORACLE.equals(this.envName)) {
            this.env = new OracleEnv(conn, user, pwd, schema);
        }
        else {
            this.env = new PostgreSQLEnv(conn, user, pwd, schema);
        }
        return this;
    }

    /**
     * Configure database.
     *
     * @param conn The JDBC connection string.
     * @param user The user id.
     * @param pwd The password.
     * @param schema The schema.
     * @param props The properties.
     * @return The instance.
     */
    public synchronized DaoEnv config(
            String conn,
            String user,
            String pwd,
            String schema,
            Properties props) {
        if (DATASOURCE.equals(this.envName)) {
            this.env = new AppSourceEnv(conn);
        }
        if (DATAPOOL.equals(this.envName)) {
            this.env = new HikariEnv(conn, user, pwd, props);
        }
        else if (HANA.equals(this.envName)) {
            this.env = new HanaEnv(conn, user, pwd, schema);
        }
        else if (MSSQL.equals(this.envName)) {
            this.env = new SQLServerEnv(conn, user, pwd, schema);
        }
        else if (ORACLE.equals(this.envName)) {
            this.env = new OracleEnv(conn, user, pwd, schema);
        }
        else {
            this.env = new PostgreSQLEnv(conn, user, pwd, schema);
        }
        return this;
    }

    /**
     * Create a connection.
     *
     * @return A connection.
     * @throws SQLException Failed to execute.
     */
    public synchronized Connection create() throws SQLException {
        return new ConnectionProxy(this.env.create());
    }

    protected abstract void initialFactory(DaoFactory factory) throws Exception;

    /**
     * Connection proxy of J2SE connection.
     *
     * @author Kyle K. Lin
     *
     */
    public static class ConnectionProxy implements Connection {

        private final Connection conn;

        ConnectionProxy(Connection conn) {
            this.conn = conn;
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return this.conn.isWrapperFor(iface);
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            return this.conn.unwrap(iface);
        }

        @Override
        public void abort(Executor executor) throws SQLException {
            this.conn.abort(executor);

        }

        @Override
        public void clearWarnings() throws SQLException {
            this.conn.clearWarnings();
        }

        @Override
        public void close() throws SQLException {
            if (!getAutoCommit()) {
                this.conn.rollback();
            }
            this.conn.close();
        }

        @Override
        public void commit() throws SQLException {
            this.conn.commit();
            this.conn.setAutoCommit(true);
        }

        @Override
        public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
            return this.conn.createArrayOf(typeName, elements);
        }

        @Override
        public Blob createBlob() throws SQLException {
            return this.conn.createBlob();
        }

        @Override
        public Clob createClob() throws SQLException {
            return this.conn.createClob();
        }

        @Override
        public NClob createNClob() throws SQLException {
            return this.conn.createNClob();
        }

        @Override
        public SQLXML createSQLXML() throws SQLException {
            return this.conn.createSQLXML();
        }

        @Override
        public Statement createStatement() throws SQLException {
            return this.conn.createStatement();
        }

        @Override
        public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
            return this.conn.createStatement(resultSetType, resultSetConcurrency);
        }

        @Override
        public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return this.conn.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        @Override
        public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
            return this.conn.createStruct(typeName, attributes);
        }

        @Override
        public boolean getAutoCommit() throws SQLException {
            return this.conn.getAutoCommit();
        }

        @Override
        public String getCatalog() throws SQLException {
            return this.conn.getCatalog();
        }

        @Override
        public Properties getClientInfo() throws SQLException {
            return this.conn.getClientInfo();
        }

        @Override
        public String getClientInfo(String name) throws SQLException {
            return this.conn.getClientInfo(name);
        }

        @Override
        public int getHoldability() throws SQLException {
            return this.conn.getHoldability();
        }

        @Override
        public DatabaseMetaData getMetaData() throws SQLException {
            return this.conn.getMetaData();
        }

        @Override
        public int getNetworkTimeout() throws SQLException {
            return this.conn.getNetworkTimeout();
        }

        @Override
        public String getSchema() throws SQLException {
            return this.conn.getSchema();
        }

        @Override
        public int getTransactionIsolation() throws SQLException {
            return this.conn.getTransactionIsolation();
        }

        @Override
        public Map<String, Class<?>> getTypeMap() throws SQLException {
            return this.conn.getTypeMap();
        }

        @Override
        public SQLWarning getWarnings() throws SQLException {
            return this.conn.getWarnings();
        }

        @Override
        public boolean isClosed() throws SQLException {
            return this.conn.isClosed();
        }

        @Override
        public boolean isReadOnly() throws SQLException {
            return this.conn.isReadOnly();
        }

        @Override
        public boolean isValid(int timeout) throws SQLException {
            return this.conn.isValid(timeout);
        }

        @Override
        public String nativeSQL(String sql) throws SQLException {
            return this.conn.nativeSQL(sql);
        }

        @Override
        public CallableStatement prepareCall(String sql) throws SQLException {
            return this.conn.prepareCall(sql);
        }

        @Override
        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
            return this.conn.prepareCall(sql, resultSetType, resultSetConcurrency);
        }

        @Override
        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return this.conn.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        @Override
        public PreparedStatement prepareStatement(String sql) throws SQLException {
            return this.conn.prepareStatement(sql);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
            return this.conn.prepareStatement(sql, autoGeneratedKeys);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
            return this.conn.prepareStatement(sql, columnIndexes);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
            return this.conn.prepareStatement(sql, columnNames);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
            return this.conn.prepareStatement(sql, resultSetType, resultSetConcurrency);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return this.conn.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        @Override
        public void releaseSavepoint(Savepoint savepoint) throws SQLException {
            this.conn.releaseSavepoint(savepoint);
        }

        @Override
        public void rollback() throws SQLException {
            this.conn.rollback();
        }

        @Override
        public void rollback(Savepoint savepoint) throws SQLException {
            this.conn.rollback(savepoint);
        }

        @Override
        public void setAutoCommit(boolean autoCommit) throws SQLException {
            this.conn.setAutoCommit(autoCommit);
        }

        @Override
        public void setCatalog(String catalog) throws SQLException {
            this.conn.setCatalog(catalog);
        }

        @Override
        public void setClientInfo(Properties properties) throws SQLClientInfoException {
            this.conn.setClientInfo(properties);
        }

        @Override
        public void setClientInfo(String name, String value) throws SQLClientInfoException {
            this.conn.setClientInfo(name, value);
        }

        @Override
        public void setHoldability(int holdability) throws SQLException {
            this.conn.setHoldability(holdability);
        }

        @Override
        public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
            this.conn.setNetworkTimeout(executor, milliseconds);
        }

        @Override
        public void setReadOnly(boolean readOnly) throws SQLException {
            this.conn.setReadOnly(readOnly);

        }

        @Override
        public Savepoint setSavepoint() throws SQLException {
            return this.conn.setSavepoint();
        }

        @Override
        public Savepoint setSavepoint(String name) throws SQLException {
            return this.conn.setSavepoint(name);
        }

        @Override
        public void setSchema(String schema) throws SQLException {
            this.conn.setSchema(schema);
        }

        @Override
        public void setTransactionIsolation(int level) throws SQLException {
            this.conn.setTransactionIsolation(level);
        }

        @Override
        public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
            this.conn.setTypeMap(map);
        }

    }

}
