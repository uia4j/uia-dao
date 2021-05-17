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
 * The common DAO implementation for the view.
 *
 * @author Kyle K. Lin
 *
 * @param <T> The DTO class type.
 */
public class ViewDao<T> {

    /**
     * The JDBC connection.
     */
    protected final Connection conn;

    /**
     * The helper of this DAO.
     */
    protected final ViewDaoHelper<T> viewHelper;

    /**
     * Constructor.
     *
     * @param conn A JDBC connection.
     * @param viewHelper A DAO helper for a specific view.
     */
    public ViewDao(Connection conn, ViewDaoHelper<T> viewHelper) {
        this.conn = conn;
        this.viewHelper = viewHelper;
    }

    /**
     * Selects all rows of the view.
     *
     * @return All rows of the view.
     * @throws SQLException Failed to execute the SQL statement.
     * @throws DaoException Failed to map to the DTO object.
     */
    public List<T> selectAll() throws SQLException, DaoException {
        String orderBy = this.viewHelper.getOrderBy();
        String sql = getSql();
        if (!orderBy.isEmpty()) {
            sql = sql + " ORDER BY " + orderBy;
        }

        try (PreparedStatement ps = this.conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                return toList(rs, Filter.ALL);
            }
        }
    }

    /**
     * Selects rows of the view.
     *
     * @param filter The filter.
     * @return Rows.
     * @throws SQLException Failed to execute the SQL statement.
     * @throws DaoException Failed to map to the DTO object.
     */
    public List<T> select(Filter filter) throws SQLException, DaoException {
        String orderBy = this.viewHelper.getOrderBy();
        String sql = getSql();
        if (!orderBy.isEmpty()) {
            sql = sql + " ORDER BY " + orderBy;
        }

        try (PreparedStatement ps = this.conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                return toList(rs, filter);
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
        SelectStatement sql = new SelectStatement(getSql())
                .where(where)
                .orderBy(this.viewHelper.getOrderBy());
        try (PreparedStatement ps = sql.prepare(this.conn)) {
            try (ResultSet rs = ps.executeQuery()) {
                return toList(rs, Filter.ALL);
            }
            catch (SQLException ex) {
                throw ex;
            }
            catch (DaoException ex2) {
                throw ex2;
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
        SelectStatement sql = new SelectStatement(getSql())
                .where(where)
                .orderBy(orders);
        try (PreparedStatement ps = sql.prepare(this.conn)) {
            try (ResultSet rs = ps.executeQuery()) {
                return toList(rs, Filter.ALL);
            }
        }
    }

    /**
     * Selects some rows with a criteria.
     *
     * @param where The where statement.
     * @param filter The filter.
     * @return Rows meet the criteria.
     * @throws SQLException Failed to execute the SQL statement.
     * @throws DaoException Failed to map to the DTO object.
     */
    public List<T> select(Where where, Filter filter) throws SQLException, DaoException {
        SelectStatement sql = new SelectStatement(getSql())
                .where(where)
                .orderBy(this.viewHelper.getOrderBy());
        try (PreparedStatement ps = sql.prepare(this.conn)) {
            try (ResultSet rs = ps.executeQuery()) {
                return toList(rs, filter);
            }
            catch (SQLException ex) {
                throw ex;
            }
            catch (DaoException ex2) {
                throw ex2;
            }
        }
    }

    /**
     * Selects some rows with a criteria.
     *
     * @param where The where statement.
     * @param filter The filter.
     * @param orders The orders.
     * @return Rows meet the criteria.
     * @throws SQLException Failed to execute the SQL statement.
     * @throws DaoException Failed to map to the DTO object.
     */
    public List<T> select(Where where, Filter filter, String orders) throws SQLException, DaoException {
        SelectStatement sql = new SelectStatement(getSql())
                .where(where)
                .orderBy(orders);
        try (PreparedStatement ps = sql.prepare(this.conn)) {
            try (ResultSet rs = ps.executeQuery()) {
                return toList(rs, filter);
            }
            catch (SQLException ex) {
                throw ex;
            }
            catch (DaoException ex2) {
                throw ex2;
            }
        }
    }

    public String getSql() {
        return this.viewHelper.forSelect().getSql();
    }

    protected String getSql(String where) {
        return this.viewHelper.forSelect().getSql() + " " + where;
    }

    protected String getSql(String where, String orderBy) {
        return this.viewHelper.forSelect().getSql() + " " + where + " ORDER BY " + orderBy;
    }

    protected List<T> toList(ResultSet rs, Filter filter) throws SQLException, DaoException {
        return this.viewHelper.forSelect().toList(rs, filter);
    }

    protected T toOne(ResultSet rs) throws SQLException, DaoException {
        return this.viewHelper.forSelect().toOne(rs);
    }
}
