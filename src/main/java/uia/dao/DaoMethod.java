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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
* The DaoMthod includes a full columns SQL statement and provide methods to set DTO object into PreparedStatement or get DTO objects from a ResultSet.
*
* @author Kyle K. Lin
*
*/
public final class DaoMethod<T> {

    private final Class<T> clz;

    private final ArrayList<DaoColumn> columns;

    private String sql;

    public DaoMethod(Class<T> clz) {
        this.clz = clz;
        this.columns = new ArrayList<>();
    }

    /**
     * Returns the SQL statement for this method.
     *
     * @return The SQL statement.
     */
    public String getSql() {
        return this.sql;
    }

    /**
     * Apply a DTO object to the SQL statement.
     *
     * @param ps The prepared statement to be setup.
     * @param obj The DTO object.
     * @throws SQLException Failed to execute the SQL statement.
     * @throws DaoException Failed to map to the DTO object.
     */
    public void fromOne(PreparedStatement ps, Object obj) throws SQLException, DaoException {
        int index = 1;
        for (DaoColumn col : this.columns) {
            try {
                col.run(obj, ps, index);
                index++;
            }
            catch (SQLException ex1) {
                System.out.println("column:" + col + " failed");
                throw ex1;
            }
            catch (DaoException ex2) {
                System.out.println("column:" + col + " failed");
                throw ex2;
            }
        }
    }

    /**
     * Convert result set to DTO object list.
     *
     * @param rs The result set.
     * @param filter The filter.
     * @return DTO object list.
     * @throws SQLException Failed to execute the SQL statement.
     * @throws DaoException Failed to map to the DTO object.
     */
    public List<T> toList(final ResultSet rs, Filter filter) throws SQLException, DaoException {
        if (filter == null) {
            filter = Filter.ALL;
        }
        ArrayList<T> result = new ArrayList<>();
        while (rs.next()) {
            if (!filter.accept(rs)) {
                continue;
            }
            int index = 1;
            try {
                T data = this.clz.newInstance();
                for (DaoColumn col : this.columns) {
                    col.run(data, rs, index);
                    index++;
                }
                result.add(data);
            }
            catch (InstantiationException | IllegalAccessException e) {
                throw new DaoException(e);
            }

        }
        return result;
    }

    /**
     * Convert result set to DTO object list.
     *
     * @param rs The result set.
     * @param filter The filter.
     * @param n The max count of result.
     * @return DTO object list.
     * @throws SQLException Failed to execute the SQL statement.
     * @throws DaoException Failed to map to the DTO object.
     */
    public List<T> toList(final ResultSet rs, Filter filter, final int n) throws SQLException, DaoException {
        if (n <= 0) {
            return toList(rs, filter);
        }
        if (filter == null) {
            filter = Filter.ALL;
        }

        ArrayList<T> result = new ArrayList<>();
        int i = 0;
        while (rs.next() && i < n) {
            try {
                if (!filter.accept(rs)) {
                    continue;
                }
                i++;
                T data = this.clz.newInstance();
                int index = 1;
                for (DaoColumn col : this.columns) {
                    col.run(data, rs, index);
                    index++;
                }
                result.add(data);
            }
            catch (InstantiationException | IllegalAccessException e) {
                throw new DaoException(e);
            }

        }
        return result;
    }

    /**
     * Convert result set to one DTO object.
     *
     * @param rs The result set.
     * @return A DTO object.
     * @throws SQLException Failed to execute the SQL statement.
     * @throws DaoException Failed to map to the DTO object.
     */
    public T toOne(ResultSet rs) throws SQLException, DaoException {
        T data = null;
        if (rs.next()) {
            try {
                data = this.clz.newInstance();
                int index = 1;
                for (DaoColumn col : this.columns) {
                    col.run(data, rs, index);
                    index++;
                }
            }
            catch (InstantiationException | IllegalAccessException e) {
                throw new DaoException(e);
            }

        }
        return data;
    }

    /**
     * Print columns. Debug only.
     *
     */
    public void println() {
        System.out.println(this.sql);
        int i = 1;
        for (DaoColumn col : this.columns) {
            System.out.println(String.format(" %2s. %s", i++, col));
        }
    }

    @Override
    public String toString() {
        return this.sql;
    }

    public void addColumn(DaoColumn column) {
        this.columns.add(column);
    }

    public void setSql(String sql) {
        this.sql = sql;
    }
}
