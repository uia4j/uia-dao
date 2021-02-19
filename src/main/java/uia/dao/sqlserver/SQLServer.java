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
package uia.dao.sqlserver;

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
import uia.dao.pg.PostgreSQLColumnType;

public class SQLServer extends AbstractDatabase {

    public SQLServer() throws SQLException {
        super(null, null, null, null, null);
    }

    public SQLServer(String host, String port, String databaseName, String user, String pwd) throws SQLException {
        super("com.microsoft.sqlserver.jdbc.SQLServerDriver", String.format("jdbc:sqlserver://%s:%s;databaseName=%s", host, port, databaseName), user, pwd, null);
    }

    public SQLServer(String host, String port, String databaseName, String user, String pwd, String schema) throws SQLException {
        super("com.microsoft.sqlserver.jdbc.SQLServerDriver", String.format("jdbc:sqlserver://%s:%s;databaseName=%s", host, port, databaseName), user, pwd, schema);
    }

    @Override
    public String selectViewScript(String viewName) throws SQLException {
        String script = null;

        try (PreparedStatement ps = this.conn.prepareStatement("SELECT name, sql FROM sqlite_master WHERE name=?")) {
            ps.setString(1, viewName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    script = rs.getString(2);
                    int i = script.indexOf("AS");
                    script = script.substring(i + 3);
                }
                return script;
            }
        }
    }

    @Override
    public String generateCreateViewSQL(String viewName, String sql) {
        String pref = this.getSchema();
        if (pref == null) {
            pref = "";
        }
        else {
            pref += ".";
        }
        return String.format("CREATE VIEW %s%s AS %n%s;", pref, viewName.toUpperCase(), sql);
    }

    @Override
    public String generateCreateTableSQL(TableType table) {
        if (table == null) {
            return null;
        }

        ArrayList<String> pks = new ArrayList<>();
        ArrayList<String> cols = new ArrayList<>();
        ArrayList<String> comments = new ArrayList<>();
        /**
        if (table.getRemark() != null) {
            comments.add(String.format("COMMENT ON TABLE %s is '%s';%n",
                    table.getTableName().toUpperCase(),
                    table.getRemark()));
        }
        */

        for (ColumnType ct : table.getColumns()) {
            if (ct.isPk()) {
                pks.add(ct.getColumnName().toUpperCase());
            }
            cols.add(prepareColumnDef(ct));
            /**
            if (ct.getRemark() != null &&
                    ct.getRemark().trim().length() > 0) {
                comments.add(String.format("COMMENT ON COLUMN %s.%s is '%s';%n",
                        table.getTableName().toUpperCase(),
                        ct.getColumnName().toUpperCase(),
                        ct.getRemark()));
            }
            */
        }

        StringBuilder sb = new StringBuilder();
        String pref = this.getSchema();
        if (pref == null) {
            pref = "";
        }
        else {
            pref += ".";
        }
        sb.append("CREATE TABLE " + pref + table.getTableName().toUpperCase() + " (\n ");
        sb.append(String.join(",\n ", cols));
        if (pks.isEmpty()) {
            sb.append("\n);\n");
        }
        else {
            String pkSQL = String.format(",%n CONSTRAINT PK_%s PRIMARY KEY (%s)%n",
                    table.getTableName().toUpperCase(),
                    String.join(",", pks));
            sb.append(pkSQL).append(");\n");
        }

        for (String comment : comments) {
            sb.append(comment);
        }

        return sb.toString();
    }

    @Override
    public String generateAlterTableSQL(String tableName, List<ColumnDiff> details) {
        return null;

    }

    @Override
    public String generateDropTableSQL(String tableName) {
        return "DROP TABLE IF EXISTS " + tableName.toUpperCase();
    }

    @Override
    public String generateDropViewSQL(String viewName) {
        return "DROP VIEW IF EXISTS " + viewName.toUpperCase();
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
                    ColumnType ct = new PostgreSQLColumnType();
                    ct.setPk(pks.contains(columnName));
                    ct.setColumnName(columnName);
                    ct.setDecimalDigits(rs.getInt("DECIMAL_DIGITS"));
                    ct.setDataTypeCode(rs.getInt("DATA_TYPE"));
                    ct.setDataTypeName(rs.getString("TYPE_NAME"));
                    ct.setNullable("1".equals(rs.getString("NULLABLE")));
                    ct.setColumnSize(rs.getInt("COLUMN_SIZE"));
                    ct.setRemark(rs.getString("REMARKS"));

                    switch (rs.getInt("DATA_TYPE")) {
                        case Types.TINYINT:
                        case Types.SMALLINT:
                        case Types.INTEGER:
                            ct.setDataType(DataType.INTEGER);
                            break;
                        case Types.BIGINT:
                            ct.setDataType(DataType.LONG);
                            break;
                        case Types.CHAR:
                        case Types.VARCHAR:
                            ct.setDataType(DataType.VARCHAR);
                            break;
                        case Types.NCHAR:
                        case Types.NVARCHAR:
                            ct.setDataType(DataType.NVARCHAR);
                            break;
                        case Types.LONGNVARCHAR:
                        case Types.CLOB:
                            ct.setDataType(DataType.CLOB);
                            break;
                        case Types.NCLOB:
                            ct.setDataType(DataType.NCLOB);
                            break;
                        case Types.BLOB:
                            ct.setDataType(DataType.BLOB);
                            break;
                        case Types.FLOAT:
                        case Types.DOUBLE:
                            ct.setDataType(DataType.DOUBLE);
                            break;
                        case Types.REAL:
                        case Types.DECIMAL:
                        case Types.NUMERIC:
                            ct.setDataType(DataType.NUMERIC);
                            break;
                        case Types.DATE:
                            ct.setDataType(DataType.DATE);
                            break;
                        case Types.TIME:
                            ct.setDataType(DataType.TIME);
                            break;
                        case Types.TIMESTAMP:
                            ct.setDataType(DataType.TIMESTAMP);
                            break;
                        case Types.TIME_WITH_TIMEZONE:
                            ct.setDataType(DataType.TIMESTAMPZ);
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
        return value.toUpperCase();
    }

    private String prepareColumnDef(ColumnType ct) {
        String type = "";
        long columnSize = ct.getColumnSize() == 0 ? 32L : ct.getColumnSize();
        switch (ct.getDataType()) {
            case LONG:
                type = "bigint";
                break;
            case NUMERIC:
            case FLOAT:
            case DOUBLE:
                if (ct.getDecimalDigits() == 0) {
                    type = "numeric";
                }
                else {
                    type = "numeric(" + columnSize + "," + ct.getDecimalDigits() + ")";
                }
                break;
            case INTEGER:
                type = "int";
                break;
            case DATE:
            case TIME:
            case TIMESTAMP:
                type = "datetime2";
                break;
            case TIMESTAMPZ:
                type = "datetimeoffset";
                break;
            case CLOB:
            case JSON:
            case NVARCHAR:
            case NVARCHAR2:
                type = "nvarchar(" + columnSize + ")";
                break;
            case VARCHAR:
            case VARCHAR2:
                type = "nvarchar(" + columnSize + ")";
                break;
            case NCLOB:
                type = "ntext";
                break;
            case BLOB:
                type = "varbinary";
                break;
            default:
                throw new NullPointerException(ct.getColumnName() + " type not found");

        }

        String nullable = "";
        if (ct.isPk() || !ct.isNullable()) {
            nullable = " NOT NULL";
        }

        return ct.getColumnName().toUpperCase() + " " + type + nullable;
    }
}
