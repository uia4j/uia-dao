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
package uia.dao.mysql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import uia.dao.AbstractDatabase;
import uia.dao.ColumnDiff;
import uia.dao.ColumnType;
import uia.dao.ColumnType.DataType;
import uia.dao.TableType;

/**
 *
 * @author Kyle K. Lin
 *
 */
public class MySQL extends AbstractDatabase {

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
        }
        catch (Exception e) {

        }
    }

    public MySQL() throws SQLException {
        super(null, null, null, null, null);
    }

    public MySQL(String host, String port, String database, String user, String pwd) throws SQLException {
        super("com.mysql.cj.jdbc.Driver", String.format("jdbc:mysql://%s:%s/%s", host, port, database), user, pwd, null);

    }

    public MySQL(String host, String port, String database, String user, String pwd, String schema) throws SQLException {
        super("com.mysql.cj.jdbc.Driver", String.format("jdbc:mysql://%s:%s/%s", host, port, database), user, pwd, schema);
    }

    @Override
    public String selectViewScript(String viewName) throws SQLException {
        String script = null;
        try (PreparedStatement ps = this.conn.prepareStatement("select VIEW_DEFINITION from INFORMATION_SCHEMA.VIEWS where TABLE_NAME=?")) {
            ps.setString(1, viewName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    script = rs.getString(1);
                }
                return script != null && script.startsWith("\n") ? script.substring(1) : script;
            }
        }
    }

    @Override
    public String generateCreateViewSQL(String viewName, String sql) {
        throw new RuntimeException("no implementation");
    }

    @Override
    public String generateCreateTableSQL(TableType table) {
        throw new RuntimeException("no implementation");
    }

    @Override
    public String generateAlterTableSQL(String tableName, List<ColumnDiff> details) {
        throw new RuntimeException("no implementation");
    }

    @Override
    public String generateDropTableSQL(String tableName) {
        throw new RuntimeException("no implementation");
    }

    @Override
    public String generateDropViewSQL(String viewName) {
        throw new RuntimeException("no implementation");
    }

    @Override
    public List<ColumnType> selectColumns(String tableName, boolean firstAsPK) throws SQLException {
        ArrayList<String> pks = new ArrayList<>();
        try (ResultSet rs = this.conn.getMetaData().getPrimaryKeys(null, this.schema, tableName)) {
            while (rs.next()) {
                pks.add(rs.getString("COLUMN_NAME"));
            }
        }

        /**
         * TABLE_CAT
         * TABLE_SCHEM
         * TABLE_NAME
         * COLUMN_NAME
         * DATA_TYPE
         * TYPE_NAME
         * COLUMN_SIZE
         * BUFFER_LENGTH
         * DECIMAL_DIGITS
         * NUM_PREC_RADIX
         * NULLABLE
         * REMARKS
         * COLUMN_DEF
         * SQL_DATA_TYPE
         * SQL_DATETIME_SUB
         * CHAR_OCTET_LENGTH
         * ORDINAL_POSITION
         * IS_NULLABLE
         * SCOPE_CATALOG
         * SCOPE_SCHEMA
         * SCOPE_TABLE
         * SOURCE_DATA_TYPE
         * IS_AUTOINCREMENT         *
         */
        List<ColumnType> cts = new ArrayList<>();
        try (ResultSet rs = this.conn.getMetaData().getColumns(null, this.schema, tableName, null)) {
            while (rs.next()) {
                if (tableName.equalsIgnoreCase(rs.getString("TABLE_NAME"))) {
                    String columnName = rs.getString("COLUMN_NAME");
                    ColumnType ct = new MySQLColumnType();
                    ct.setPk(pks.contains(columnName));
                    ct.setColumnName(columnName);
                    ct.setDecimalDigits(rs.getInt("DECIMAL_DIGITS"));
                    ct.setDataTypeCode(rs.getInt("DATA_TYPE"));
                    ct.setDataTypeName(rs.getString("TYPE_NAME"));
                    ct.setNullable("1".equals(rs.getString("NULLABLE")));
                    ct.setColumnSize(rs.getInt("COLUMN_SIZE"));
                    ct.setRemark(rs.getString("REMARKS"));
                    ct.setDefaultValue(rs.getString("COLUMN_DEF"));

                    switch (rs.getInt("DATA_TYPE")) {   // PostgreSQL TYPE
                        case Types.BINARY:              // bytea
                        case Types.VARBINARY:           // bytea
                            ct.setDataType(DataType.BLOB);
                            break;
                        case Types.CHAR:                // bpchar
                            if (ct.getColumnSize() > Integer.MAX_VALUE / 2) {
                                ct.setDataType(DataType.CLOB);
                            }
                            else {
                                ct.setDataType(DataType.VARCHAR2);
                            }
                            break;
                        case Types.DECIMAL:             // numeric
                        case Types.NUMERIC:             // decimal
                            ct.setDataType(DataType.NUMERIC);
                            break;
                        case Types.TINYINT:             // int2
                        case Types.SMALLINT:            // int2
                            ct.setDataType(DataType.INTEGER);
                            break;
                        case Types.INTEGER:             // int4
                            ct.setDataType(DataType.INTEGER);
                            break;
                        case Types.BIGINT:              // int8, oid(?)
                            ct.setDataType(DataType.LONG);
                            break;
                        case Types.REAL:                // float4
                            break;
                        case Types.FLOAT:               // float4
                        case Types.DOUBLE:              // float8
                            ct.setDataType(DataType.DOUBLE);
                            break;
                        case Types.VARCHAR:
                        case Types.LONGVARCHAR:         // character varying,text
                            if (ct.getColumnSize() > Integer.MAX_VALUE / 2) {
                                ct.setDataType(DataType.CLOB);
                            }
                            else {
                                ct.setDataType(DataType.VARCHAR2);
                            }
                            break;
                        case Types.DATE:                // date
                            ct.setDataType(DataType.DATE);
                            break;
                        case Types.TIME:                // time, timez
                            ct.setDataType(DataType.TIME);
                            break;
                        case Types.TIMESTAMP:           // timestamp, timestamp without timezone, timestamptz
                            if (isAlwaysTimestampZ() || "timestamptz".equals(ct.getDataTypeName())) {
                                ct.setDataType(DataType.TIMESTAMPZ);
                            }
                            else {
                                ct.setDataType(DataType.TIMESTAMP);
                            }
                            break;
                        case Types.TIME_WITH_TIMEZONE:  // timestamp with timezone
                            ct.setDataType(DataType.TIMESTAMPZ);
                            break;
                        case Types.BIT:                 // bit
                            ct.setDataType(DataType.BIT);
                            break;
                        case 1111:						// JSON
                            ct.setDataType(DataType.JSON);
                            break;
                        default:
                            ct.setDataType(DataType.UNDEFINED);
                            break;

                    }
                    cts.add(ct);
                }
            }
        }
        if (pks.isEmpty() && firstAsPK && !cts.isEmpty()) {
            cts.get(0).setPk(true);
            pks.add(cts.get(0).getColumnName());
        }

        return cts;
    }

    @Override
    protected String upperOrLower(String value) {
        return value.toLowerCase();
    }
}
