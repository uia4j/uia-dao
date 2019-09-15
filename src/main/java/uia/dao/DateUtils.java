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
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author Kyle K. Lin
 *
 */
public final class DateUtils {

    private DateUtils() {
    }

    public static void setDate(PreparedStatement ps, int index, Date value) throws SQLException {
        if (value == null) {
            ps.setNull(index, java.sql.Types.TIMESTAMP);
        }
        else {
            ps.setTimestamp(index, new java.sql.Timestamp(value.getTime()));
        }
    }

    public static void setDateTz(PreparedStatement ps, int index, Date value) throws SQLException {
        if (value == null) {
            ps.setNull(index, java.sql.Types.TIMESTAMP);
        }
        else {
            ps.setTimestamp(index, new java.sql.Timestamp(value.getTime() - TimeZone.getDefault().getRawOffset()));
        }
    }

    public static Date getDate(ResultSet rs, int index) throws SQLException {
        if (rs.getObject(index) == null) {
            return null;
        }
        return new Date(rs.getTimestamp(index).getTime());
    }

    public static Date getDateTz(ResultSet rs, int index) throws SQLException {
        if (rs.getObject(index) == null) {
            return null;
        }
        return new Date(rs.getTimestamp(index).getTime() + TimeZone.getDefault().getRawOffset());
    }
}
