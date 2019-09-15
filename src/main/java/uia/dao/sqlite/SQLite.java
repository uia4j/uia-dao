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
package uia.dao.sqlite;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import uia.dao.AbstractDatabase;
import uia.dao.ColumnType;
import uia.dao.TableType;
import uia.dao.ColumnType.DataType;
import uia.dao.pg.PostgreSQLColumnType;

public class SQLite extends AbstractDatabase {

    static {
        try {
            Class.forName("org.sqlite.JDBC").newInstance();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SQLite() throws SQLException {
        super(null, null, null, null, null);
    }

    public SQLite(String file) throws SQLException {
        /** jdbc:sqlite:{file} */
        super("org.sqlite.JDBC", "jdbc:sqlite:" + file, null, null, null);
    }

    @Override
    public int createView(String viewName, String sql) throws SQLException {
        String script = String.format("CREATE VIEW \"%s\" AS %n%s", viewName.toLowerCase(), sql);
        try (PreparedStatement ps = this.conn.prepareStatement(script)) {
            return ps.executeUpdate();
        }
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
        return String.format("CREATE VIEW \"%s\" AS %n%s", viewName.toLowerCase(), sql);
    }

    @Override
    public String generateCreateTableSQL(TableType table) {
        if (table == null) {
            return null;
        }

        ArrayList<String> pks = new ArrayList<>();
        ArrayList<String> cols = new ArrayList<>();
        ArrayList<String> comments = new ArrayList<>();
        if (table.getRemark() != null) {
            comments.add(String.format("COMMENT ON TABLE %s is '%s';%n",
                    table.getTableName().toLowerCase(),
                    table.getRemark()));
        }

        for (ColumnType ct : table.getColumns()) {
            if (ct.isPk()) {
                pks.add(ct.getColumnName().toLowerCase());
            }
            cols.add(prepareColumnDef(ct));
            if (ct.getRemark() != null &&
                    ct.getRemark().trim().length() > 0) {
                comments.add(String.format("COMMENT ON COLUMN %s.%s is '%s';%n",
                        table.getTableName().toLowerCase(),
                        ct.getColumnName().toLowerCase(),
                        ct.getRemark()));
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE \"" + table.getTableName().toLowerCase() + "\"\n(\n");
        sb.append(String.join(",\n", cols));
        if (pks.isEmpty()) {
            sb.append("\n);\n");
        }
        else {
            String pkSQL = String.format(",%n CONSTRAINT %s_pkey PRIMARY KEY (%s)%n",
                    table.getTableName().toLowerCase(),
                    String.join(",", pks));
            sb.append(pkSQL).append(");\n");
        }

        for (String comment : comments) {
            sb.append(comment);
        }

        return sb.toString();
    }

    @Override
    public String generateAlterTableSQL(String tableName, List<ColumnType> columns) {
        ArrayList<String> cols = new ArrayList<>();
        for (ColumnType column : columns) {
            cols.add(prepareColumnDef(column));
        }
        return "ALTER TABLE " + tableName + " ADD (\n" + String.join(",\n", cols) + "\n)";
    }

    @Override
    public List<ColumnType> selectColumns(String tableName, boolean firstAsPK) throws SQLException {
        ArrayList<String> pks = new ArrayList<>();
        try (ResultSet rs = this.conn.getMetaData().getPrimaryKeys(null, null, tableName)) {
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
        try (ResultSet rs = this.conn.getMetaData().getColumns(null, null, tableName, null)) {
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
                        case Types.BIGINT:
                            ct.setDataType(DataType.INTEGER);
                            break;
                        case Types.VARCHAR:
                        case Types.NVARCHAR:
                        case Types.CLOB:
                        case Types.NCLOB:
                            ct.setDataType(DataType.VARCHAR2);
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
                        default:
                            ct.setDataType(DataType.OTHERS);
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

    private String prepareColumnDef(ColumnType ct) {
        String type = "";
        switch (ct.getDataType()) {
            case LONG:
                type = "INT";
                break;
            case NUMERIC:
                type = "NUMERIC";
                break;
            case FLOAT:
            case DOUBLE:
                type = "REAL";
                break;
            case INTEGER:
                type = "INT";
                break;
            case DATE:
            case TIME:
            case TIMESTAMP:
                type = "TEXT";
                break;
            case NVARCHAR:
            case NVARCHAR2:
            case VARCHAR:
            case VARCHAR2:
                type = "TEXT";
                break;
            case CLOB:
            case NCLOB:
                type = "TEXT";
                break;
            case BLOB:
                type = "BLOB";
                break;
            default:
                throw new NullPointerException(ct.getColumnName() + " type not found");

        }

        String nullable = "";
        if (ct.isPk() || !ct.isNullable()) {
            nullable = " NOT NULL";
        }

        return " \"" + ct.getColumnName().toLowerCase() + "\" " + type + nullable;
    }
}
