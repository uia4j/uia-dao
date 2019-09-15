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

import java.sql.ResultSet;
import java.sql.SQLException;

/**
*
* @author Kyle K. Lin
*
*/
public interface DaoColumnReader {

    public Object read(ResultSet rs, int index) throws SQLException;

    /**
     * Returns a string or "" if null.
     *
     * @param rs The result.
     * @param index The index.
     * @return Result.
     * @throws SQLException Failed to read the value.
     */
    public static Object null2Empty(ResultSet rs, int index) throws SQLException {
        String r = rs.getString(index);
        return r == null ? "" : r;
    }

    public static Object empty2Null(ResultSet rs, int index) throws SQLException {
        String r = rs.getString(index);
        return r != null && r.isEmpty() ? null : r;
    }
}
