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

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import uia.dao.ColumnType.DataType;
import uia.dao.annotation.ColumnInfo;
import uia.dao.annotation.TableInfo;

/**
 * The helper of the TableDao.
 *
 * @author Kyle K. Lin
 *
 */
public final class TableDaoHelper<T> {

    private final DaoFactory factory;

    private final String tableClassName;

    private final String tableName;

    private final ArrayList<String> primaryKeys;

    private final DaoMethod<T> insert;

    private final DaoMethod<T> update;

    private final DaoMethod<T> delete;

    private final DaoMethod<T> select;

    private final DaoMethod<T> selectWithAlias;

    private final String wherePK;

    private final String orderBy;

    private final TableType tableType;

    TableDaoHelper(DaoFactory factory, Class<T> clz) {
        this.factory = factory;;
        TableInfo ti = clz.getDeclaredAnnotation(TableInfo.class);
        if (ti == null) {
            throw new NullPointerException(clz.getName() + ": @TableInfo annotation not found");
        }

        this.tableClassName = clz.getName();
        this.tableName = factory.readSchema(ti.schema()) + ti.name();
        this.orderBy = ti.orderBy().trim().isEmpty() ? "" : ti.orderBy();

        this.insert = new DaoMethod<>(clz);
        this.update = new DaoMethod<>(clz);
        this.delete = new DaoMethod<>(clz);
        this.select = new DaoMethod<>(clz);
        this.selectWithAlias = new DaoMethod<>(clz);
        this.primaryKeys = new ArrayList<>();

        ArrayList<String> prikeyColNames = new ArrayList<>();
        ArrayList<String> insertColNames = new ArrayList<>();
        ArrayList<String> updateColNames = new ArrayList<>();
        ArrayList<String> selectColNames = new ArrayList<>();
        Field[] fs = clz.getDeclaredFields();

        ArrayList<ColumnType> cts = new ArrayList<>();
        ArrayList<DaoColumn> pks = new ArrayList<>();
        for (Field f : fs) {
            ColumnInfo ci = f.getDeclaredAnnotation(ColumnInfo.class);

            if (ci != null) {
                String cvrtName = ci.converter();
                if (cvrtName.isEmpty()) {
                    if (ci.sqlType() == DataType.JSON) {
                        cvrtName = "json";
                    }
                    else {
                        cvrtName = f.getType().getSimpleName();
                    }
                }

                DataType dataType = ci.sqlType();
                if (dataType == DataType.UNDEFINED) {
                    dataType = factory.getDataType(cvrtName);
                }

                // ColumnType
                ColumnType ct = new ColumnType();
                ct.setPk(ci.primaryKey());
                ct.setColumnName(ci.name());
                ct.setColumnSize(ci.length());
                ct.setDataType(dataType);
                ct.setDataTypeCode(dataType.sqlType);
                ct.setDecimalDigits(ci.scale());
                ct.setNullable(!ci.primaryKey());
                ct.setRemark(ci.remark());
                cts.add(ct);

                // DaoColumn
                DaoColumnReader r = factory.getColumnReader(cvrtName);
                if (r == null) {
                    throw new NullPointerException(String.format("Column:%s reader:%s not found", ct.getColumnName(), cvrtName));
                }
                DaoColumnWriter w = factory.getColumnWriter(cvrtName);
                if (w == null) {
                    throw new NullPointerException(String.format("Column:%s writer:%s not found", ct.getColumnName(), cvrtName));
                }
                DaoColumn column = new DaoColumn(f, r, w);

                if (ci.primaryKey()) {
                    this.primaryKeys.add(ci.name());
                    this.delete.addColumn(column);
                    prikeyColNames.add(ci.name() + "=?");
                    pks.add(column);
                }
                else {
                    this.update.addColumn(column);
                    updateColNames.add(ci.name() + "=?");
                }
                this.insert.addColumn(column);
                this.select.addColumn(column);
                this.selectWithAlias.addColumn(column);

                insertColNames.add("?");
                selectColNames.add(ci.name());

            }
        }
        for (DaoColumn col : pks) {
            this.update.addColumn(col);
        }

        this.tableType = new TableType(ti.name(), ti.remark(), cts, true);
        this.insert.setSql(String.format("INSERT INTO %s(%s) VALUES (%s)",
                this.tableName,
                String.join(",", selectColNames),
                String.join(",", insertColNames)));
        if (!updateColNames.isEmpty()) {
            this.update.setSql(String.format("UPDATE %s SET %s WHERE %s",
                    this.tableName,
                    String.join(",", updateColNames),
                    String.join(" AND ", prikeyColNames)));
        }
        this.delete.setSql(String.format("DELETE FROM %s ",
                this.tableName));
        this.select.setSql(String.format("SELECT %s FROM %s ",
                String.join(",", selectColNames),
                this.tableName));
        this.selectWithAlias.setSql(String.format("SELECT x.%s FROM %s AS x ",
                String.join(",x.", selectColNames),
                this.tableName));
        this.wherePK = String.join(" AND ", prikeyColNames);
    }

    public DaoFactory getFactory() {
        return this.factory;
    }

    public TableType getTableType() {
        return this.tableType;
    }

    /**
     * Returns the class name of the table..
     *
     * @return The class name.
     */
    public String getTableClassName() {
        return this.tableClassName;
    }

    /**
     * Returns the table name.
     *
     * @return The table name.
     */
    public String getTableName() {
        return this.tableName;
    }

    public String[] getPrimaryKeys() {
        return this.primaryKeys.toArray(new String[0]);
    }

    /**
     * Returns a method for INSERT which contains all columns of the table.<br>
     * The SQL will be 'INSERT INTO table_name(pk1,pk2,c1,c2...) VALUES (?,?,?,?...)'.
     *
     * @return The INSERT method.
     */
    public DaoMethod<T> forInsert() {
        return this.insert;
    }

    /**
     * Returns a method for UPDATE contains all columns of the table.<br>
     * The SQL will be 'UPDATE table_name SET c1=?,c2=?... WHERE pk1=? AND pk2=?'.
     *
     * @return The UPDATE method.
     */
    public DaoMethod<T> forUpdate() {
        if (this.update == null) {
            throw new UnsupportedOperationException(this.tableName + " does not support update feature");
        }
        return this.update;
    }

    /**
     * Returns a method for DELETE.<br>
     * The SQL will be 'DELETE FROM table_name'.
     *
     * @return The DELETE method.
     */
    public DaoMethod<T> forDelete() {
        return this.delete;
    }

    /**
     * Returns a method for SELECT which contains all columns of the table.<br>
     * The SQL will be 'SELECT pk1,pk2,c1,c2... FROM table_name'.
     *
     * @return The SELECT method.
     */
    public DaoMethod<T> forSelect() {
        return this.select;
    }

    public T toOne(ResultSet rs) throws SQLException, DaoException {
        return this.select.toOne(rs);
    }

    public List<T> toList(ResultSet rs) throws SQLException, DaoException {
        return this.select.toList(rs, null);
    }

    /**
     * Returns a method for SELECT which contains all columns of the table with alias name 'x'.<br>
     * The SQL will be 'SELECT x.pk1,x.pk2,vc1,x.c2... FROM table_name AS x'.
     *
     * @return The SELECT method.
     */
    public DaoMethod<T> forSelectX() {
        return this.selectWithAlias;
    }

    public String getOrderBy() {
        return this.orderBy;
    }

    public String forWherePK() {
        return this.wherePK;
    }

}
