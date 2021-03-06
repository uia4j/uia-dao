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
package uia.dao.where.conditions;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

/**
 *
 * @author Kyle K. Lin
 *
 */
public interface ConditionType {

    /**
     * Return statement.
     *
     * @return Statement.
     */
    public String getStatement();

    /**
     * Accept this condition.
     *
     * @param ps Statement instance.
     * @param index Index to be set.
     * @return Current index.
     * @throws SQLException Failed to execute.
     */
    public default int accpet(PreparedStatement ps, int index) throws SQLException {
        return index;
    }
    
    public static void apply(PreparedStatement ps, int index, Object value) throws SQLException {
        if (value instanceof Date) {
            ps.setTimestamp(index, new Timestamp(((Date) value).getTime()));
        }
        else {
            ps.setObject(index, value);
        }
    }
}
