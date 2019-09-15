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
package uia.dao.where;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * The WHERE statement builder.
 *
 * @author Kyle K. Lin
 *
 */
public abstract class Where {

    /**
     * Test conditions exist or not.
     *
     * @return Result.
     */
    public abstract boolean hasConditions();

    /**
     * Generate the statement.
     *
     * @return The statement.
     */
    public abstract String generate();

    /**
     * Accept where statement.
     *
     * @param ps Statement instance.
     * @param index Index to set.
     * @return Next index.
     * @throws SQLException Failed to execute.
     */
    public abstract int accept(PreparedStatement ps, int index) throws SQLException;

    /**
     * Create a simple AND statement.
     *
     * @return AND statement.
     */
    public static SimpleWhere simpleAnd() {
        return new SimpleWhere(" and ");
    }

    /**
     * Create a simple OR statement.
     *
     * @return OR statement.
     */
    public static SimpleWhere simpleOr() {
        return new SimpleWhere(" or ");
    }

    /**
     * Create a AND statement.
     *
     * @param ws Where list.
     * @return AND statement.
     */
    public static WhereAnd and(Where... ws) {
        WhereAnd where = new WhereAnd();
        for (Where w : ws) {
            where.add(w);
        }
        return where;
    }

    /**
     * Create a OR statement.
     *
     * @param ws Where list.
     * @return OR statement.
     */
    public static WhereOr or(Where... ws) {
        WhereOr where = new WhereOr();
        for (Where w : ws) {
            where.add(w);
        }
        return where;
    }

    /**
     * Test if value is empty.
     *
     * @param value Value.
     * @return Result.
     */
    protected boolean isEmpty(Object value) {
        return value == null || value.toString().trim().length() == 0;
    }

    /**
     * Returns WHERE statement.
     *
     * @param where Where statement.
     * @param paramValues parameters and values.
     * @return Result.
     */
    public static String toString(String where, List<Object> paramValues) {
        if (where == null) {
            return "";
        }

        String sql = where;
        int index = 0;
        for (int i = 0, c = paramValues.size(); i < c; i++) {
            index = sql.indexOf("?");
            if (index > 0) {
                String v = "" + paramValues.get(i);
                sql = sql.substring(0, index) + "'" + v + "'" + sql.substring(index + 1);
                index = index + v.length() + 2;
            }
        }
        return sql;
    }

}
