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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import uia.dao.where.Where;

/**
 * The common DAO implementation for the table.
 *
 * @author Kyle K. Lin
 *
 * @param <T> The DTO class type.
 */
public class TableDao<T> {

    /**
     * The JDBC connection.
     */
    protected final Connection conn;

    /**
     * The helper of this DAO.
     */
    protected final TableDaoHelper<T> tableHelper;

    /**
     * Constructor.
     *
     * @param conn A JDBC connection.
     * @param tableHelper A DAO helper for a specific table.
     */
    public TableDao(Connection conn, TableDaoHelper<T> tableHelper) {
        this.conn = conn;
        this.tableHelper = tableHelper;
    }

    /**
     * Inserts a row.
     *
     * @param data The row.
     * @return Result.
     * @throws SQLException Failed to insert.
     * @throws DaoException Failed or ORM.
     */
    public int insert(T data) throws SQLException, DaoException {
        DaoMethod<T> method = this.tableHelper.forInsert();
        try (PreparedStatement ps = this.conn.prepareStatement(method.getSql())) {
            method.fromOne(ps, data);
            return ps.executeUpdate();
        }
    }

    /**
     * Inserts rows.
     *
     * @param data The rows.
     * @return Result.
     * @throws SQLException Failed to insert.
     * @throws DaoException Failed or ORM.
     */
    public int insert(List<T> data) throws SQLException, DaoException {
        if (data.isEmpty()) {
            return 0;
        }

        DaoMethod<T> method = this.tableHelper.forInsert();
        try (PreparedStatement ps = this.conn.prepareStatement(method.getSql())) {
            for (T t : data) {
                method.fromOne(ps, t);
                ps.addBatch();
            }
            return ps.executeUpdate();
        }
    }

    /**
     * Updates a row.
     *
     * @param data The rows.
     * @return Result.
     * @throws SQLException Failed to update.
     * @throws DaoException Failed or ORM.
     */
    public int update(T data) throws SQLException, DaoException {
        DaoMethod<T> method = this.tableHelper.forUpdate();
        try (PreparedStatement ps = this.conn.prepareStatement(method.getSql())) {
            method.fromOne(ps, data);
            return ps.executeUpdate();
        }
    }

    /**
     * Updates rows.
     *
     * @param data The rows.
     * @return Result.
     * @throws SQLException Failed to update.
     * @throws DaoException Failed or ORM.
     */
    public int update(List<T> data) throws SQLException, DaoException {
        if (data.isEmpty()) {
            return 0;
        }

        DaoMethod<T> method = this.tableHelper.forUpdate();
        try (PreparedStatement ps = this.conn.prepareStatement(method.getSql())) {
            for (T t : data) {
                method.fromOne(ps, t);
                ps.addBatch();
            }
            return ps.executeUpdate();
        }
    }

    /**
     * Deletes a row.
     *
     * @param pks Values of primary keys.
     * @return Result.
     * @throws SQLException Failed to update.
     */
    public int deleteByPK(Object... pks) throws SQLException {
        if (pks.length == 0) {
            return 0;
        }

        DaoMethod<T> method = this.tableHelper.forDelete();
        try (PreparedStatement ps = this.conn.prepareStatement(method.getSql() + "WHERE " + this.tableHelper.forWherePK())) {
            for (int i = 0; i < pks.length; i++) {
                ps.setObject(i + 1, pks[i]);
            }
            return ps.executeUpdate();
        }
    }

    /**
     * Selects all rows of the table.
     *
     * @return All rows of the table.
     * @throws SQLException Failed to execute the SQL statement.
     * @throws DaoException Failed to map to the DTO object.
     */
    public List<T> selectAll() throws SQLException, DaoException {
        DaoMethod<T> method = this.tableHelper.forSelect();
        try (PreparedStatement ps = this.conn.prepareStatement(method.getSql())) {
            try (ResultSet rs = ps.executeQuery()) {
                return method.toList(rs);
            }
        }
    }

    /**
     * Selects a row of the table.
     *
     * @param pks Values of primary keys.
     * @return A row of the table.
     * @throws SQLException Failed to update.
     * @throws DaoException Failed or ORM.
     */
    public T selectByPK(Object... pks) throws SQLException, DaoException {
        if (pks.length == 0) {
            return null;
        }

        DaoMethod<T> method = this.tableHelper.forSelect();
        try (PreparedStatement ps = this.conn.prepareStatement(method.getSql() + "WHERE " + this.tableHelper.forWherePK())) {
            for (int i = 0; i < pks.length; i++) {
                ps.setObject(i + 1, pks[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return method.toOne(rs);
            }
        }
    }

    /**
     * Selects some rows with a criteria.
     *
     * @param where The where statement.
     * @return Rows meet the criteria.
     * @throws SQLException Failed to execute the SQL statement.
     * @throws DaoException Failed to map to the DTO object.
     */
    public List<T> select(Where where) throws SQLException, DaoException {
        DaoMethod<T> method = this.tableHelper.forSelect();
        SelectStatement sql = new SelectStatement(method.getSql())
                .where(where);
        try (PreparedStatement ps = sql.prepare(this.conn)) {
            try (ResultSet rs = ps.executeQuery()) {
                return method.toList(rs);
            }
        }
    }

    /**
     * Selects some rows with a criteria.
     *
     * @param where The where statement.
     * @param orders The orders.
     * @return Rows meet the criteria.
     * @throws SQLException Failed to execute the SQL statement.
     * @throws DaoException Failed to map to the DTO object.
     */
    public List<T> select(Where where, String orders) throws SQLException, DaoException {
        DaoMethod<T> method = this.tableHelper.forSelect();
        SelectStatement sql = new SelectStatement(method.getSql())
                .where(where)
                .orderBy(orders);
        try (PreparedStatement ps = sql.prepare(this.conn)) {
            try (ResultSet rs = ps.executeQuery()) {
                return method.toList(rs);
            }
        }
    }
}
