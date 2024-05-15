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
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.sql.DataSource;

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
        this.schema = this.conn.getSchema();
    }

    /**
     * Constructor.
     *
     * @param driverName The driver class.
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
            //this.dataSource = createDataSource(driverName, url, user, pwd);
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
        String[] scripts = generateCreateTableSQL(table).split(";;");
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
    }

    @Override
    public int createView(String viewName, String sql) throws SQLException {
        String script = generateCreateViewSQL(viewName, sql);
        try (Statement st = this.conn.createStatement()) {
            return st.executeUpdate(script);
        }
    }

    @Override
    public int dropView(String viewName) throws SQLException {
        String script = generateDropViewSQL(viewName);
        try (Statement st = this.conn.createStatement()) {
            return st.executeUpdate(script);
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

    @Override
    public Map<String, IndexInfo> selectIndexScripts(String tableName) throws SQLException {
        TreeMap<String, IndexInfo> scripts = new TreeMap<>();
        try (ResultSet rs = this.conn.getMetaData().getIndexInfo(null, this.schema, upperOrLower(tableName), true, false)) {
            while (rs.next()) {
                String indexName = rs.getString("INDEX_NAME").toUpperCase();
                boolean unique = !rs.getBoolean("NON_UNIQUE");
                int pos = rs.getInt("ORDINAL_POSITION") - 1;
                String colName = rs.getString("COLUMN_NAME").toUpperCase();
                String order = rs.getString("ASC_OR_DESC").toUpperCase();

                IndexInfo ii = scripts.get(indexName);
                if (ii == null) {
                    ii = new IndexInfo(tableName, indexName, unique);
                    scripts.put(indexName, ii);
                }
                ii.addColumn(pos, colName, order);
            }
        }
        try (ResultSet rs = this.conn.getMetaData().getIndexInfo(null, this.schema, upperOrLower(tableName), false, true)) {
            while (rs.next()) {
                String indexName = rs.getString("INDEX_NAME").toUpperCase();
                boolean unique = !rs.getBoolean("NON_UNIQUE");
                int pos = rs.getInt("ORDINAL_POSITION") - 1;
                String colName = rs.getString("COLUMN_NAME").toUpperCase();
                String order = rs.getString("ASC_OR_DESC").toUpperCase();

                IndexInfo ii = scripts.get(indexName);
                if (ii == null) {
                    ii = new IndexInfo(tableName, indexName, unique);
                    scripts.put(indexName, ii);
                }
                ii.addColumn(pos, colName, order);
            }
        }
        return scripts;
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

    @Override
    public List<Object[]> query(String sql) throws SQLException {
        ArrayList<Object[]> result = new ArrayList<>();
        Statement stat = this.conn.createStatement();
        try (ResultSet rs = stat.executeQuery(sql)) {
            int c = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                Object[] values = new Object[c];
                for (int i = 0; i < c; i++) {
                    values[i] = rs.getObject(i + 1);
                }
                result.add(values);
            }
        }
        return result;
    }

    @Override
    public int copy(String tableName, Database to, int cache) throws SQLException {
        cache = Math.min(Math.max(100, cache), 20000);

        TableType table = selectTable(tableName, false);
        String insertSQL = table.generateInsertSQL();  // wired

        Statement stat = getConnection().createStatement();
        try (ResultSet rs = stat.executeQuery(table.generateSelectSQL())) {
            to.execute("TRUNCATE TABLE " + tableName);

            int cols = rs.getMetaData().getColumnCount();
            int[] types = new int[cols];
            for (int i = 0; i < types.length; i++) {
                types[i] = rs.getMetaData().getColumnType(i + 1);
            }

            int count = 0;
            List<List<Object>> values = new ArrayList<>();
            while (rs.next()) {
                count++;
                ArrayList<Object> row = new ArrayList<>();
                for (int i = 1; i <= cols; i++) {
                    if (types[i - 1] == Types.NCLOB || types[i - 1] == Types.CLOB) {
                        row.add(rs.getString(i));
                    }
                    else {
                        row.add(rs.getObject(i));
                    }
                }
                values.add(row);

                if (values.size() == cache) {
                    to.executeBatch(insertSQL, values);
                    values.clear();
                }
            }
            to.executeBatch(insertSQL, values);
            return count;
        }
    }

    @Override
    public int copy(String tableName, Database to, int cache, String where) throws SQLException {
        cache = Math.min(Math.max(100, cache), 2000);

        TableType table = selectTable(tableName, false);
        String insertSQL = table.generateInsertSQL();

        Statement stat = getConnection().createStatement();
        try (ResultSet rs = stat.executeQuery(table.generateSelectSQL() + " " + where)) {
            to.execute("delete from " + tableName + " " + where);

            int cols = rs.getMetaData().getColumnCount();
            int[] types = new int[cols];
            for (int i = 0; i < types.length; i++) {
                types[i] = rs.getMetaData().getColumnType(i + 1);
            }

            int count = 0;
            List<List<Object>> values = new ArrayList<>();
            while (rs.next()) {
                count++;
                ArrayList<Object> row = new ArrayList<>();
                for (int i = 1; i <= cols; i++) {
                    if (types[i - 1] == Types.NCLOB || types[i - 1] == Types.CLOB) {
                        row.add(rs.getString(i));
                    }
                    else {
                        row.add(rs.getObject(i));
                    }
                }
                values.add(row);

                if (values.size() == cache) {
                    to.executeBatch(insertSQL, values);
                    values.clear();
                }
            }
            to.executeBatch(insertSQL, values);
            return count;
        }
    }

    public static class IndexInfo {

        public final String tableName;

        public final String indexName;

        public final boolean unique;

        public final List<String> columns;

        public final List<String> orders;

        IndexInfo(String tableName, String indexName, boolean unique) {
            this.tableName = tableName;
            this.indexName = indexName;
            this.unique = unique;
            this.columns = new ArrayList<>();
            this.orders = new ArrayList<>();
        }

        public String getTableName() {
            return this.tableName;
        }

        public String getIndexName() {
            return this.indexName;
        }

        public List<String> getOrders() {
            return this.orders;
        }

        public boolean isUnique() {
            return this.unique;
        }

        public List<String> getColumns() {
            return this.columns;
        }

        public void addColumn(int index, String columnName, String orders) {
            if (this.columns.contains(columnName)) {
                return;
            }
            this.columns.add(index, columnName);
            this.orders.add(index, orders);
        }

        public boolean same(IndexInfo ii) {
            if (!this.tableName.equals(ii.getTableName())) {
                return false;
            }
            if (!this.indexName.equals(ii.getIndexName())) {
                return false;
            }
            if (this.unique != ii.isUnique()) {
                return false;
            }
            if (!this.columns.toString().equals(ii.getColumns().toString())) {
                return false;
            }
            if (!this.orders.toString().equals(ii.getOrders().toString())) {
                return false;
            }
            return true;
        }

        public String script() {
            String cols = String.format("%s %s",
                    this.columns.get(0),
                    "D".equals(this.orders.get(0)) ? "DESC" : "A".equals(this.orders.get(0)) ? "ASC" : "");
            for (int i = 1; i < this.columns.size(); i++) {
                cols += ",";
                cols += String.format("%s %s",
                        this.columns.get(i),
                        "D".equals(this.orders.get(i)) ? "DESC" : "A".equals(this.orders.get(i)) ? "ASC" : "");
            }
            return String.format("%s INDEX %s ON %s (%s);",
                    this.unique ? "CREATE UNIQUE" : "CREATE",
                    this.indexName,
                    this.tableName,
                    cols);
        }

        @Override
        public String toString() {
            return this.indexName + ":" + this.columns;
        }
    }
}
