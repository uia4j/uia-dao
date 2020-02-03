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
import java.util.ArrayList;

import uia.dao.annotation.ColumnInfo;
import uia.dao.annotation.TableInfo;

/**
 * The helper of the TableDao.
 *
 * @author Kyle K. Lin
 *
 */
public final class TableDaoHelper<T> {

    private final String tableClassName;

    private final String tableName;

    private final ArrayList<String> primaryKeys;

    private final DaoMethod<T> insert;

    private final DaoMethod<T> update;

    private final DaoMethod<T> delete;

    private final DaoMethod<T> select;

    private final String wherePK;

    private final String orderBy;

    TableDaoHelper(DaoFactory factory, Class<T> clz) {
        TableInfo ti = clz.getDeclaredAnnotation(TableInfo.class);

        this.tableClassName = clz.getName();
        this.tableName = factory.readSchema(ti.schema()) + ti.name();
        this.orderBy = ti.orderBy().trim().isEmpty() ? "" : ti.orderBy();

        this.insert = new DaoMethod<>(clz);
        this.update = new DaoMethod<>(clz);
        this.delete = new DaoMethod<>(clz);
        this.select = new DaoMethod<>(clz);
        this.primaryKeys = new ArrayList<>();

        ArrayList<String> prikeyColNames = new ArrayList<>();
        ArrayList<String> insertColNames = new ArrayList<>();
        ArrayList<String> updateColNames = new ArrayList<>();
        ArrayList<String> selectColNames = new ArrayList<>();
        Field[] fs = clz.getDeclaredFields();

        ArrayList<DaoColumn> pks = new ArrayList<>();
        for (Field f : fs) {
            ColumnInfo ci = f.getDeclaredAnnotation(ColumnInfo.class);
            if (ci != null) {
                String typeName = ci.typeName();
                if (typeName.isEmpty()) {
                    typeName = f.getType().getSimpleName();
                }

                DaoColumn column = new DaoColumn(
                        f,
                        factory.getColumnReader(typeName),
                        factory.getColumnWriter(typeName));

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

                insertColNames.add("?");
                selectColNames.add(ci.name());

            }
        }
        for (DaoColumn col : pks) {
            this.update.addColumn(col);
        }

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
        this.wherePK = String.join(" AND ", prikeyColNames);
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

    public String getOrderBy() {
        return this.orderBy;
    }

    String forWherePK() {
        return this.wherePK;
    }

}
