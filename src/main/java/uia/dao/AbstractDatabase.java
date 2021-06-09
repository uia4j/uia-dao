/*******************************************************************************
 * Copyright 2019 UIA
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package uia.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Abstract implementation for all databases.
 *
 * @author Kyle K. Lin
 *
 */
public abstract class AbstractDatabase implements Database {

    private static final String TABLE = "TABLE";

    private static final String VIEW = "VIEW";

    private final String url;

    protected final Connection conn;

    protected String schema;

    private DataSource dataSource;

    private boolean alwaysNVarchar;

    private boolean alwaysTimestampZ;

    /**
     * Constructor.
     *
     * @param dataSource The data source.
     * @throws SQLException Failed to initial.
     */
    protected AbstractDatabase(DataSource dataSource) throws SQLException {
        this.url = "DataSource:" + getClass().getSimpleName();
        this.alwaysNVarchar = false;
        this.alwaysTimestampZ = false;
        this.conn = dataSource.getConnection();
        this.dataSource = dataSource;
        this.schema = this.conn.getSchema();
    }

    /**
     * Constructor.
     *
     * @param driverName The driver calss.
     * @param url The JDBC connection string.
     * @param user The user id.
     * @param pwd The password.
     * @param schema The schema.
     * @throws SQLException Failed to initial.
     */
    protected AbstractDatabase(String driverName, String url, String user, String pwd, String schema) throws SQLException {
        this.url = url;
        this.alwaysNVarchar = false;
        this.alwaysTimestampZ = false;
        if (url != null) {
            this.conn = DriverManager.getConnection(url, user, pwd);
            this.dataSource = createDataSource(driverName, url, user, pwd);
            this.schema = schema;
            if (this.schema != null) {
                this.conn.setSchema(schema);
            }
        }
        else {
            this.conn = null;
            // this.dataSource = null;
            this.schema = schema;
        }
    }

    @Override
    public boolean isAlwaysNVarchar() {
        return this.alwaysNVarchar;
    }

    @Override
    public void setAlwaysNVarchar(boolean alwaysNVarchar) {
        this.alwaysNVarchar = alwaysNVarchar;
    }

    @Override
    public boolean isAlwaysTimestampZ() {
        return this.alwaysTimestampZ;
    }

    @Override
    public void setAlwaysTimestampZ(boolean alwaysTimestampZ) {
        this.alwaysTimestampZ = alwaysTimestampZ;
    }

    @Override
    public void close() throws SQLException {
        if (this.conn != null) {
            this.conn.close();
        }
    }

    @Override
    public Connection getConnection() {
        return this.conn;
    }

    @Override
    public Connection createConnection() throws SQLException {
        return this.dataSource.getConnection();
    }

    @Override
    public String getSchema() {
        return this.schema;
    }

    @Override
    public void setSchema(String schema) {
        this.schema = schema;
    }

    @Override
    public List<String> selectTableNames() throws SQLException {
        ArrayList<String> tables = new ArrayList<>();
        try (ResultSet rs = this.conn.getMetaData().getTables(null, this.schema, null, new String[] { TABLE })) {
            while (rs.next()) {
                tables.add(rs.getString(3));
            }
            return tables;
        }
    }

    @Override
    public List<String> selectTableNames(String prefix) throws SQLException {
        if (prefix == null || prefix.trim().length() == 0) {
            return selectTableNames();
        }

        ArrayList<String> tables = new ArrayList<>();
        try (ResultSet rs = this.conn.getMetaData().getTables(null, this.schema, upperOrLower(prefix) + "%", new String[] { TABLE })) {
            while (rs.next()) {
                String tn = rs.getString(3);
                if (tn.startsWith(prefix)) {
                    tables.add(tn);
                }
            }
        }
        return tables;
    }

    @Override
    public List<String> selectViewNames() throws SQLException {
        ArrayList<String> tables = new ArrayList<>();
        try (ResultSet rs = this.conn.getMetaData().getTables(null, this.schema, null, new String[] { VIEW })) {
            while (rs.next()) {
                tables.add(rs.getString(3));
            }
            return tables;
        }
    }

    @Override
    public List<String> selectViewNames(String prefix) throws SQLException {
        if (prefix == null || prefix.trim().length() == 0) {
            return selectViewNames();
        }

        ArrayList<String> views = new ArrayList<>();
        try (ResultSet rs = this.conn.getMetaData().getTables(null, this.schema, upperOrLower(prefix) + "%", new String[] { VIEW })) {
            while (rs.next()) {
                String vn = rs.getString(3);
                if (vn.startsWith(prefix)) {
                    views.add(vn);
                }
            }
        }
        return views;
    }

    @Override
    public boolean exists(String tableOrView) throws SQLException {
        try (ResultSet rs = this.conn.getMetaData().getTables(null, this.schema, upperOrLower(tableOrView), new String[] { TABLE, VIEW })) {
            return rs.next();
        }
    }

    @Override
    public TableType selectTable(String tableOrView, boolean firstAsPk) throws SQLException {
        boolean table = true;
        String comment = null;
        try (ResultSet rs = this.conn.getMetaData().getTables(null, this.schema, upperOrLower(tableOrView), new String[] { TABLE, VIEW })) {
            if (!rs.next()) {
                return null;
            }
            table = "TABLE".equalsIgnoreCase(rs.getString(4));
            comment = rs.getString("REMARKS");
        }

        List<ColumnType> columns = selectColumns(upperOrLower(tableOrView), firstAsPk);
        return columns.isEmpty() ? null : new TableType(upperOrLower(tableOrView), comment, columns, table);
    }

    @Override
    public int createTable(TableType table) throws SQLException {
        String[] scripts = generateCreateTableSQL(table).split(";");
        try (Statement st = this.conn.createStatement()) {
            for (String script : scripts) {
                if (!script.trim().isEmpty()) {
                    st.executeUpdate(script);
                }
            }
        }
        return 0;
    }

    @Override
    public int dropTable(String tableName) throws SQLException {
        String script = generateDropTableSQL(tableName);
        try (Statement st = this.conn.createStatement()) {
            return st.executeUpdate(script);
        }
        catch (SQLException ex) {
            System.out.println("failed to execute: " + script);
            throw ex;
        }
    }

    @Override
    public int createView(String viewName, String sql) throws SQLException {
        String script = this.generateCreateViewSQL(viewName, sql);
        try (Statement st = this.conn.createStatement()) {
            return st.executeUpdate(script);
        }
        catch (SQLException ex) {
            System.out.println("failed to execute: " + script);
            throw ex;
        }
    }

    @Override
    public int dropView(String viewName) throws SQLException {
        String script = generateDropViewSQL(viewName);
        try (Statement st = this.conn.createStatement()) {
            return st.executeUpdate(script);
        }
        catch (SQLException ex) {
            System.out.println("failed to execute: " + script);
            throw ex;
        }
    }

    @Override
    public boolean execute(String sql) throws SQLException {
        try (java.sql.Statement state = this.conn.createStatement()) {
            sql = fix(sql);
            if (sql != null) {
                return state.execute(sql);
            }
            else {
                return false;
            }
        }
    }

    @Override
    public int[] executeBatch(List<String> sqls) throws SQLException {
        try (java.sql.Statement state = this.conn.createStatement()) {
            for (String sql : sqls) {
                sql = fix(sql);
                if (sql != null) {
                    System.out.println(sql);
                    state.addBatch(sql);
                }
            }
            return state.executeBatch();
        }
    }

    @Override
    public int[] executeBatch(String sql, List<List<Object>> rows) throws SQLException {
        try (PreparedStatement ps = this.conn.prepareStatement(sql)) {
            for (List<Object> row : rows) {
                int i = 1;
                for (Object col : row) {
                    ps.setObject(i++, col);
                }
                ps.addBatch();
            }
            return ps.executeBatch();
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " url:" + this.url;
    }

    /**
     * Change value to upper case or lower case. Implementation for a database can change the value depending on its naming rule.
     *
     * @param value The table name or view name.
     * @return Result.
     */
    protected abstract String upperOrLower(String value);

    protected String fix(String sql) {
        if (sql != null && sql.startsWith("\n")) {
            sql = sql.substring(1);
        }
        if (sql == null || sql.trim().isEmpty()) {
            return null;
        }
        else {
            return sql;
        }
    }

    private DataSource createDataSource(String driverName, String connectUrl, String user, String pwd) {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(driverName);
        config.setJdbcUrl(connectUrl);
        config.setUsername(user);
        config.setPassword(pwd);
        config.setMaximumPoolSize(200);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "512");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "1024");

        return new HikariDataSource(config);
    }
}
