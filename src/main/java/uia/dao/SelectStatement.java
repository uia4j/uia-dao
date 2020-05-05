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
import java.sql.SQLException;

import uia.dao.where.Where;

/**
 * The statement used to select/update/delete a table.
 * 
 * @author Kyle K. Lin
 *
 */
public class SelectStatement {

    private final String selectSql;

    private Where where;

    private String groups;

    private String orders;

    /**
     * Constructor.
     * 
     * @param selectSql The 'select' part.
     */
    public SelectStatement(String selectSql) {
        this.selectSql = selectSql;
    }

    public void reset() {
        this.groups = null;
        this.orders = null;
    }

    /**
     * Sets the 'where' statement.
     * 
     * @param where The 'where' statement.
     * @return The instance.
     */
    public SelectStatement where(Where where) {
        this.where = where;
        return this;
    }

    /**
     * Sets the 'group by' statement.
     * 
     * @param groups The 'group by' statement.
     * @return The instance.
     */
    public SelectStatement groupBy(String groups) {
        this.groups = groups;
        return this;
    }

    /**
     * Sets the 'order by' statement.
     * 
     * @param orders The 'order by' statement.
     * @return The instance.
     */
    public SelectStatement orderBy(String orders) {
        this.orders = orders;
        return this;
    }

    public PreparedStatement prepare(Connection conn) throws SQLException {
        final PreparedStatement ps;

        String whereSQL = this.where == null ? null : this.where.generate();
        if (whereSQL == null || whereSQL.length() == 0) {
            String sql = String.format("%s%s%s",
                    this.selectSql,
                    groupBy(),
                    orderBy());
            ps = conn.prepareStatement(sql);
        }
        else {
            String sql = String.format("%s where %s%s%s",
                    this.selectSql,
                    whereSQL,
                    groupBy(),
                    orderBy());
            ps = conn.prepareStatement(sql);
            this.where.accept(ps, 1);
        }

        return ps;
    }

    protected String groupBy() {
        return this.groups == null || this.groups.trim().isEmpty()
                ? ""
                : " group by " + this.groups;
    }

    protected String orderBy() {
        return this.orders == null || this.orders.trim().isEmpty()
                ? ""
                : " order by " + this.orders;
    }

    protected boolean isEmpty(Object value) {
        return value == null || value.toString().trim().length() == 0;
    }

}
