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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.reflections.Reflections;

import uia.dao.ColumnType.DataType;
import uia.dao.annotation.DaoInfo;
import uia.dao.annotation.TableInfo;
import uia.dao.annotation.ViewInfo;

/**
* The DAO factory.
*
* @author Kyle K. Lin
*
*/
public final class DaoFactory {

    private String defaultSchema;

    private final TreeMap<String, DataType> dataTypes;

    private final TreeMap<String, DaoColumnReader> readers;

    private final TreeMap<String, DaoColumnWriter> writers;

    private final DaoColumnReader objectReader;

    private final DaoColumnWriter objectWriter;

    private final TreeMap<String, TableDaoHelper<?>> daoTables;

    private final TreeMap<String, ViewDaoHelper<?>> daoViews;

    /**
     * Constructor.
     *
     * @param useTz Enable time zone for Date or not.
     *
     */
    public DaoFactory(boolean useTz) {
        this.dataTypes = new TreeMap<>();
        this.dataTypes.put("short", DataType.INTEGER);
        this.dataTypes.put("int", DataType.INTEGER);
        this.dataTypes.put("integer", DataType.INTEGER);
        this.dataTypes.put("long", DataType.LONG);
        this.dataTypes.put("bigdecimal", DataType.NUMERIC);
        this.dataTypes.put("string", DataType.NVARCHAR2);
        this.dataTypes.put("date", useTz ? DataType.TIMESTAMPZ : DataType.TIMESTAMP);
        this.dataTypes.put("clob", DataType.CLOB);
        this.dataTypes.put("byte[]", DataType.BLOB);

        this.readers = new TreeMap<>();
        this.readers.put("short", this::readShort);
        this.readers.put("int", this::readInt);
        this.readers.put("integer", this::readInt);
        this.readers.put("long", this::readLong);
        this.readers.put("bigdecimal", this::readBigDecimal);
        this.readers.put("string", this::readString);
        this.readers.put("stringE2N", DaoColumnReader::empty2Null);
        this.readers.put("stirngN2E", DaoColumnReader::null2Empty);
        this.readers.put("date", useTz ? this::readDateTz : this::readDate);
        this.readers.put("clob", this::readString);
        this.readers.put("byte[]", this::readBytes);
        this.objectReader = this::readObject;

        this.writers = new TreeMap<>();
        this.writers.put("short", this::writeShort);
        this.writers.put("int", this::writeInt);
        this.writers.put("integer", this::writeInt);
        this.writers.put("long", this::writeLong);
        this.writers.put("bigdecimal", this::writeBigDecimal);
        this.writers.put("string", this::writeString);
        this.writers.put("stringE2N", DaoColumnWriter::empty2Null);
        this.writers.put("date", useTz ? this::writeDateTz : this::writeDate);
        this.writers.put("clob", this::writeClob);
        this.writers.put("byte[]", this::writeBytes);
        this.objectWriter = this::writeObject;

        this.daoTables = new TreeMap<>();
        this.daoViews = new TreeMap<>();
    }

    public DaoSession createSession(Connection conn) {
        return new DaoSession(this, conn);
    }

    /**
     * Create a DAO for a table.
     *
     * @param conn The connection.
     * @param clz The DTO class type of a table.
     * @param <T> The type of DTO class.
     * @return The TableDao object.
     */
    public <T> TableDao<T> creaeTableDao(Class<T> clz, Connection conn) {
        TableDaoHelper<T> helper = forTable(clz);
        return helper == null
                ? null
                : new TableDao<T>(conn, helper);
    }

    /**
     * Create a DAO for a view.
     *
     * @param conn The connection.
     * @param clz The DTO class type of a view.
     * @param <T> The type of DTO class.
     * @return The ViewDao object.
     */
    public <T> ViewDao<T> createViewDao(Class<T> clz, Connection conn) {
        ViewDaoHelper<T> helper = forView(clz);
        return helper == null
                ? null
                : new ViewDao<T>(conn, helper);
    }

    public <T extends TableDao<?>> T proxyTableDao(Class<T> daoClz, Connection conn) throws Exception {
        DaoInfo dao = daoClz.getDeclaredAnnotation(DaoInfo.class);
        if (dao == null) {
            throw new NullPointerException("@DaoInfo not found");
        }

        TableDaoHelper<?> tableHelper = forTable(dao.type());
        if (tableHelper != null) {
            return new ProxyDao().bind(daoClz, conn, tableHelper);
        }
        return null;
    }

    public <T extends ViewDao<?>> T proxyViewDao(Class<T> daoClz, Connection conn) throws Exception {
        DaoInfo dao = daoClz.getDeclaredAnnotation(DaoInfo.class);
        if (dao == null) {
            throw new NullPointerException("@DaoInfo not found");
        }

        ViewDaoHelper<?> viewHelper = forView(dao.type());
        if (viewHelper != null) {
            return new ProxyDao().bind(daoClz, conn, viewHelper);
        }
        return null;
    }

    public String getDefaultSchema() {
        return this.defaultSchema;
    }

    public void setDefaultSchema(String defaultSchema) {
        this.defaultSchema = defaultSchema;
    }

    /**
     * Loads definitions of DAO.
     *
     * @param packageName The package name.
     * @throws DaoException Failed to load.
     */
    public void load(String packageName) throws DaoException {
        load(packageName, null);
    }

    public TableType getTableType(Class<?> clz) {
        TableDaoHelper<?> helper = forTable(clz);
        return helper == null ? null : helper.getTableType();
    }

    public String getViewCode(Class<?> clz) {
        ViewDaoHelper<?> helper = this.forView(clz);
        return helper == null ? null : helper.getCode();
    }

    /**
     * Loads definitions of DAO.
     *
     * @param packageName The package name.
     * @param loader The class loader.
     * @throws DaoException Failed to load.
     */
    public void load(String packageName, ClassLoader loader) throws DaoException {
        try {
            Reflections ref = new Reflections(packageName);
            // tables
            Set<Class<?>> tables = ref.getTypesAnnotatedWith(TableInfo.class, true);
            for (Class<?> t : tables) {
                this.daoTables.put(t.getName(), new TableDaoHelper<>(this, t));
            }
            // views
            Set<Class<?>> views = ref.getTypesAnnotatedWith(ViewInfo.class, true);
            for (Class<?> v : views) {
                this.daoViews.put(v.getName(), new ViewDaoHelper<>(this, v));
            }
        }
        catch (Exception ex) {
            throw new DaoException(ex);
        }
    }

    /**
     * Loads definitions of DAO.
     *
     * @param t Table class.
     */
    public void addTable(Class<?> t) {
        if (!this.daoTables.containsKey(t.getName())) {
            this.daoTables.put(t.getName(), new TableDaoHelper<>(this, t));
        }
    }

    /**
     * Loads definitions of DAO.
     *
     * @param v View class.
     */
    public void addView(Class<?> v) {
        if (!this.daoViews.containsKey(v.getName())) {
            this.daoViews.put(v.getName(), new ViewDaoHelper<>(this, v));
        }
    }

    /**
     * Returns table names.
     *
     * @return The table names.
     */
    public Set<String> getTables() {
        return this.daoTables.keySet();
    }

    /**
     * Returns view names.
     *
     * @return The view names.
     */
    public Set<String> getViews() {
        return this.daoViews.keySet();
    }

    /**
     * Returns DAO helper for a table.
     *
     * @param clz The DTO class type of a table.
     * @param <T> The type of DTO class.
     * @return The DAO helper for the table.
     */
    @SuppressWarnings("unchecked")
    public <T> TableDaoHelper<T> forTable(Class<T> clz) {
        addTable(clz);
        return (TableDaoHelper<T>) this.daoTables.get(clz.getName());
    }

    /**
     * Returns DAO helper for a view.
     *
     * @param clz The DTO class type of a view.
     * @param <T> The type of DTO class.
     * @return The DAO helper for the view.
     */
    @SuppressWarnings("unchecked")
    public <T> ViewDaoHelper<T> forView(Class<T> clz) {
        addView(clz);
        return (ViewDaoHelper<T>) this.daoViews.get(clz.getName());
    }

    /**
     * Adds a column reader for a specific type.
     *
     * @param typeName The type name using in the ColumnInfo annotation.
     * @param reader The reader.
     */
    public void addColumnReader(String typeName, DaoColumnReader reader) {
        this.readers.put(typeName.toLowerCase(), reader);
    }

    /**
     * Adds a column writer for a specific type.
     *
     * @param typeName The type name using in the ColumnInfo annotation.
     * @param writer The writer.
     */
    public void addColumnWriter(String typeName, DaoColumnWriter writer) {
        this.writers.put(typeName.toLowerCase(), writer);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Map<String, String> test(Connection conn) {
        Map<String, String> result = new TreeMap<>();
        for (TableDaoHelper<?> helper : this.daoTables.values()) {
            try {
                int size = new TableDao(conn, helper).selectAll().size();
                result.put(helper.getTableName(), helper.getTableClassName() + ", rows:" + size);
            }
            catch (Exception ex) {
                result.put(helper.getTableName(), helper.getTableClassName() + ", failed:" + ex.getMessage());
            }
        }
        for (ViewDaoHelper<?> helper : this.daoViews.values()) {
            result.put(helper.getViewName(), helper.getViewClassName());
            try {
                int size = new ViewDao(conn, helper).selectAll().size();
                result.put(helper.getViewName(), helper.getViewClassName() + ", rows:" + size);
            }
            catch (Exception ex) {
                result.put(helper.getViewName(), helper.getViewClassName() + ", failed:" + ex.getMessage());
            }
        }
        return result;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public boolean test(Connection conn, Class<?> clz) throws SQLException, DaoException {
        TableDaoHelper<?> helper1 = this.daoTables.get(clz.getName());
        if (helper1 != null) {
            System.out.println(helper1.forSelect().getSql());
            System.out.println("  rows:" + new TableDao(conn, helper1).selectAll().size());
            return true;
        }

        ViewDaoHelper<?> helper2 = this.daoViews.get(clz.getName());
        if (helper2 != null) {
            System.out.println(helper2.forSelect().getSql());
            System.out.println("  rows:" + new ViewDao(conn, helper2).selectAll().size());
            return true;
        }

        return false;
    }

    String readSchema(String schema) {
        if (schema == null || schema.isEmpty()) {
            return this.defaultSchema == null ? "" : this.defaultSchema + ".";
        }
        else {
            return schema + ".";
        }
    }

    DataType getDataType(String typeName) {
        DataType type = this.dataTypes.get(typeName.toLowerCase());
        return type == null ? DataType.NVARCHAR2 : type;
    }

    DaoColumnReader getColumnReader(String typeName) {
        DaoColumnReader reader = this.readers.get(typeName.toLowerCase());
        return reader == null ? this.objectReader : reader;
    }

    DaoColumnWriter getColumnWriter(Class<?> type) {
        DaoColumnWriter writer = this.writers.get(type.getSimpleName().toLowerCase());
        return writer == null ? this.objectWriter : writer;
    }

    DaoColumnWriter getColumnWriter(String typeName) {
        DaoColumnWriter writer = this.writers.get(typeName.toLowerCase());
        return writer == null ? this.objectWriter : writer;
    }

    private Object readShort(ResultSet rs, int index) throws SQLException {
        return rs.getShort(index);
    }

    private Object readInt(ResultSet rs, int index) throws SQLException {
        return rs.getInt(index);
    }

    private Object readLong(ResultSet rs, int index) throws SQLException {
        return rs.getLong(index);
    }

    private Object readBigDecimal(ResultSet rs, int index) throws SQLException {
        return rs.getBigDecimal(index);
    }

    private Object readString(ResultSet rs, int index) throws SQLException {
        return rs.getString(index);
    }

    private Date readDate(ResultSet rs, int index) throws SQLException {
        return DateUtils.getDate(rs, index);
    }

    private Date readDateTz(ResultSet rs, int index) throws SQLException {
        return DateUtils.getDateTz(rs, index);
    }

    private Object readObject(ResultSet rs, int index) throws SQLException {
        return rs.getObject(index);
    }

    private Object readBytes(ResultSet rs, int index) throws SQLException {
        return rs.getBytes(index);
    }

    private void writeShort(PreparedStatement ps, int index, Object value) throws SQLException {
        ps.setShort(index, (short) value);
    }

    private void writeInt(PreparedStatement ps, int index, Object value) throws SQLException {
        ps.setInt(index, (int) value);
    }

    private void writeLong(PreparedStatement ps, int index, Object value) throws SQLException {
        ps.setLong(index, (long) value);
    }

    private void writeBigDecimal(PreparedStatement ps, int index, Object value) throws SQLException {
        ps.setBigDecimal(index, (BigDecimal) value);
    }

    private void writeString(PreparedStatement ps, int index, Object value) throws SQLException {
        ps.setString(index, value == null ? null : value.toString());
    }

    private void writeDate(PreparedStatement ps, int index, Object value) throws SQLException {
        DateUtils.setDate(ps, index, (Date) value);
    }

    private void writeDateTz(PreparedStatement ps, int index, Object value) throws SQLException {
        DateUtils.setDateTz(ps, index, (Date) value);
    }

    private void writeObject(PreparedStatement ps, int index, Object value) throws SQLException {
        ps.setObject(index, value);
    }

    private void writeClob(PreparedStatement ps, int index, Object value) throws SQLException {
        String content = (String) value;
        try (StringReader stringReader = new StringReader(content)) {
            ps.setCharacterStream(index, stringReader, content.length());
        }
    }

    private void writeBytes(PreparedStatement ps, int index, Object value) throws SQLException {
        byte[] content = (byte[]) value;
        try (InputStream is = new ByteArrayInputStream(content)) {
            ps.setBinaryStream(index, is, content.length);
        }
        catch (IOException e) {
            throw new SQLException("Column:" + index + " failed to convert to InputStream");
        }
    }
}
