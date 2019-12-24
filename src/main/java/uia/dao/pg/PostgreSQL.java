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
package uia.dao.pg;

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
public class PostgreSQL extends AbstractDatabase {

    static {
        try {
            Class.forName("org.postgresql.Driver").newInstance();
        }
        catch (Exception e) {

        }
    }

    public PostgreSQL() throws SQLException {
        super(null, null, null, null, null);
    }

    public PostgreSQL(String host, String port, String database, String user, String pwd) throws SQLException {
        super("org.postgresql.Driver", String.format("jdbc:postgresql://%s:%s/%s", host, port, database), user, pwd, "public");
    }

    public PostgreSQL(String host, String port, String database, String user, String pwd, String schema) throws SQLException {
        super("org.postgresql.Driver", String.format("jdbc:postgresql://%s:%s/%s", host, port, database), user, pwd, schema);
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

        try (PreparedStatement ps = this.conn.prepareStatement("select pg_get_viewdef(?, true)")) {
            ps.setString(1, viewName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    script = rs.getString(1);
                    script = script.replace("::text", "").trim();
                    script = script.substring(0, script.length() - 1);
                }
                return script;
            }
        }
    }

    @Override
    public String generateCreateViewSQL(String viewName, String sql) {
        return String.format("CREATE VIEW \"%s\" AS %n%s;", viewName.toLowerCase(), sql);
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
    public String generateAlterTableSQL(String tableName, List<ColumnDiff> details) {
        ArrayList<String> cmd = new ArrayList<>();
        for (ColumnDiff detail : details) {
            switch (detail.actionType) {
                case ADD:
                    cmd.add("ADD COLUMN " + this.prepareColumnDef(detail.column));
                    break;
                case ALTER:
                    if (detail.alterType == ColumnDiff.AlterType.DATA_TYPE) {
                        cmd.add(String.format("ALTER COLUMN %s TYPE %s", detail.column.getColumnName(), dbType(detail.column)));
                    }
                    else {
                        if (detail.column.isNullable()) {
                            cmd.add(String.format("ALTER COLUMN %s DROP NOT NULL", detail.column.getColumnName()));
                        }
                        else {
                            cmd.add(String.format("ALTER COLUMN %s SET NOT NULL", detail.column.getColumnName()));
                        }
                    }
                    break;
                case DROP:
                    cmd.add("DROP COLUMN " + detail.column.getColumnName());
                    break;
            }
        }
        return "ALTER TABLE " + tableName + "\n  " + String.join(",\n  ", cmd) + ";\n";
    }

    @Override
    public String generateDropTableSQL(String tableName) {
        return "DROP TABLE IF EXISTS " + tableName.toLowerCase() + ";";
    }

    @Override
    public String generateDropViewSQL(String viewName) {
        return "DROP VIEW IF EXISTS " + viewName.toLowerCase() + ";";
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
                        case Types.BINARY:      // bytea
                        case Types.VARBINARY:   // bytea
                            ct.setDataType(DataType.BLOB);
                            break;
                        case Types.CHAR:        // bpchar
                            if (ct.getColumnSize() > Integer.MAX_VALUE / 2) {
                                ct.setDataType(DataType.CLOB);
                            }
                            else {
                                ct.setDataType(DataType.VARCHAR2);
                            }
                            break;
                        case Types.DECIMAL:     // numeric
                        case Types.NUMERIC:     // decimal
                            ct.setDataType(DataType.NUMERIC);
                            break;
                        case Types.TINYINT:     // int2
                        case Types.SMALLINT:    // int2
                            ct.setDataType(DataType.INTEGER);
                            break;
                        case Types.INTEGER:     // int4
                        case Types.BIGINT:      // int8, oid(?)
                            ct.setDataType(DataType.LONG);
                            break;
                        case Types.REAL:        // float4
                            break;
                        case Types.FLOAT:       // float4
                        case Types.DOUBLE:      // float8
                            ct.setDataType(DataType.DOUBLE);
                            break;
                        case Types.VARCHAR:
                        case Types.LONGVARCHAR: // character varying,text
                            if (ct.getColumnSize() > Integer.MAX_VALUE / 2) {
                                ct.setDataType(DataType.CLOB);
                            }
                            else {
                                ct.setDataType(DataType.VARCHAR2);
                            }
                            break;
                        case Types.DATE:        // date
                            ct.setDataType(DataType.DATE);
                            break;
                        case Types.TIME:        // time, timez
                            ct.setDataType(DataType.TIME);
                            break;
                        case Types.TIMESTAMP:   // timestamp, timestampz
                            ct.setDataType(DataType.TIMESTAMP);
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
        String type = dbType(ct);
        String nullable = "";
        if (ct.isPk() || !ct.isNullable()) {
            nullable = " NOT NULL";
        }

        return " \"" + ct.getColumnName().toLowerCase() + "\" " + type + nullable;
    }

    private String dbType(ColumnType ct) {
        String type = "";
        long columnSize = ct.getColumnSize() == 0 ? 32L : ct.getColumnSize();
        switch (ct.getDataType()) {
            case LONG:
                type = "bigint";
                break;
            case NUMERIC:
                if (ct.getDecimalDigits() == 0) {
                    type = "numeric";
                }
                else {
                    type = "numeric(" + columnSize + "," + ct.getDecimalDigits() + ")";
                }
                break;
            case FLOAT:
                type = "real";
                break;
            case DOUBLE:
                type = "double precision";
                break;
            case INTEGER:
                type = "integer";
                break;
            case DATE:
                type = "date";
                break;
            case TIME:
                type = "time without time zone";
                break;
            case TIMESTAMP:
                type = "timestamp without time zone";
                break;
            case NVARCHAR:
            case NVARCHAR2:
            case VARCHAR:
            case VARCHAR2:
                type = "character varying(" + columnSize + ")";
                break;
            case CLOB:
            case NCLOB:
                type = "text";
                break;
            case BLOB:
                type = "bytea";
                break;
            default:
                throw new NullPointerException(ct.getColumnName() + " type not found:" + ct.getDataTypeName());
        }
        return type;
    }
}
