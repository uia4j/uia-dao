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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import uia.dao.annotation.TableInfo;
import uia.dao.annotation.ViewInfo;

/**
* The DAO factory.
*
* @author Kyle K. Lin
*
*/
public final class DaoFactory {

    private final TreeMap<String, DaoColumnReader> readers;

    private final TreeMap<String, DaoColumnWriter> writers;

    private final DaoColumnReader objectReader;

    private final DaoColumnWriter objectWriter;

    private final TreeMap<String, TableDaoHelper<?>> daoTables;

    private final TreeMap<String, ViewDaoHelper<?>> daoViews;

    /**
     * Constructor.
     *
     */
    public DaoFactory() {
        this.readers = new TreeMap<>();
        this.readers.put("short", this::readShort);
        this.readers.put("int", this::readInt);
        this.readers.put("long", this::readLong);
        this.readers.put("bigdecimal", this::readBigDecimal);
        this.readers.put("string", this::readString);
        this.readers.put("stringE2N", DaoColumnReader::empty2Null);
        this.readers.put("stirngN2E", DaoColumnReader::null2Empty);
        this.readers.put("date", this::readDate);
        this.readers.put("clob", this::readString);
        this.readers.put("byte[]", this::readBytes);
        this.objectReader = this::readObject;

        this.writers = new TreeMap<>();
        this.writers.put("short", this::writeShort);
        this.writers.put("int", this::writeInt);
        this.writers.put("long", this::writeLong);
        this.writers.put("bigdecimal", this::writeBigDecimal);
        this.writers.put("string", this::writeString);
        this.writers.put("stringE2N", DaoColumnWriter::empty2Null);
        this.writers.put("date", this::writeDate);
        this.writers.put("clob", this::writeClob);
        this.writers.put("byte[]", this::writeBytes);
        this.objectWriter = this::writeObject;

        this.daoTables = new TreeMap<>();
        this.daoViews = new TreeMap<>();
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

    /**
     * Loads definitions of DAO.
     *
     * @param packageName The package name.
     * @param loader The class loader.
     * @throws DaoException Failed to load.
     */
    public void load(String packageName, ClassLoader loader) throws DaoException {
        try {
            List<Class<?>> tables = Reflections.findClasses(packageName, TableInfo.class, loader);
            for (Class<?> t : tables) {
                this.daoTables.put(t.getName(), new TableDaoHelper<>(this, t));
            }

            List<Class<?>> views = Reflections.findClasses(packageName, ViewInfo.class, loader);
            for (Class<?> v : views) {
                this.daoViews.put(v.getName(), new ViewDaoHelper<>(this, v));
            }
        }
        catch (Exception ex) {
            throw new DaoException(ex);
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
     * @return The DAO helper for the table.
     */
    @SuppressWarnings("unchecked")
    public <T> TableDaoHelper<T> forTable(Class<T> clz) {
        return (TableDaoHelper<T>) this.daoTables.get(clz.getName());
    }

    /**
     * Returns DAO helper for a view.
     *
     * @param clz The DTO class type of a vie.
     * @return The DAO helper for the view.
     */
    @SuppressWarnings("unchecked")
    public <T> ViewDaoHelper<T> forView(Class<T> clz) {
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

    DaoColumnReader getColumnReader(String typeName) {
        DaoColumnReader reader = this.readers.get(typeName.toLowerCase());
        return reader == null ? this.objectReader : reader;
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
            ps.setBinaryStream(index, is);
        }
        catch (IOException e) {
            throw new SQLException("Column:" + index + " failed to convert to InputStream");
        }
    }
}
