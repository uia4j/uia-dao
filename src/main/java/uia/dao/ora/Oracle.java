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
package uia.dao.ora;

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
public class Oracle extends AbstractDatabase {

    static {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
        }
        catch (Exception e) {

        }
    }

    public Oracle() throws SQLException {
        super(null, null, null, null, null);
    }

    public Oracle(String host, String port, String service, String user, String pwd) throws SQLException {
        // jdbc:oracle:thin:@host:port:SID
        super("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@" + host + ":" + port + "/" + service, user, pwd, null);
        setSchema(user.toUpperCase());
    }

    @Override
    public String selectViewScript(String viewName) throws SQLException {
        String script = null;
        try (PreparedStatement ps = this.conn.prepareStatement("select DBMS_METADATA.GET_DDL('VIEW',?) from DUAL")) {
            ps.setString(1, viewName.toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    script = rs.getString(1);
                    int i = script.indexOf(") AS");
                    script = script.substring(i + 5, script.length()).trim();
                    script = script.trim();
                    script = script.substring(1, script.length() - 1);
                }
                return script != null && script.startsWith("\n") ? script.substring(1) : script;
            }
        }
    }

    @Override
    public String generateCreateViewSQL(String viewName, String sql) {
        return String.format("CREATE VIEW \"%s\" AS (%n%s)", viewName.toUpperCase(), sql);
    }

    @Override
    public String generateCreateTableSQL(TableType table) {
        if (table == null) {
            return null;
        }

        String tableName = "\"" + table.getTableName() + "\"";
        if (this.schema != null) {
            tableName = "\"" + this.schema + "\"." + tableName;
        }
        tableName = tableName.toUpperCase();

        ArrayList<String> pks = new ArrayList<>();
        ArrayList<String> cols = new ArrayList<>();
        ArrayList<String> comments = new ArrayList<>();
        if (table.getRemark() != null) {
            comments.add(String.format("COMMENT ON TABLE %s is '%s';%n",
                    tableName,
                    table.getRemark()));
        }

        for (ColumnType ct : table.getColumns()) {
            if (ct.isPk()) {
                pks.add(ct.getColumnName().toUpperCase());
            }
            cols.add(prepareColumnDef(ct));
            if (ct.getRemark() != null && ct.getRemark().trim().length() > 0) {
                comments.add(String.format("COMMENT ON COLUMN %s.\"%s\" is '%s';%n",
                        tableName,
                        ct.getColumnName().toUpperCase(),
                        ct.getRemark()));
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE " + tableName + " \n(\n");
        sb.append(String.join(",\n", cols));
        if (pks.isEmpty()) {
            sb.append("\n);\n");
        }
        else {
            String pkSQL = String.format(",%n CONSTRAINT \"%s_PKEY\" PRIMARY KEY (\"%s\")%n",
                    table.getTableName().toUpperCase(),
                    String.join("\",\"", pks));
            sb.append(pkSQL).append(");\n");
        }

        for (String comment : comments) {
            sb.append(comment);
        }

        return sb.toString();
    }

    @Override
    public String generateAlterTableSQL(String tableName, List<ColumnDiff> details) {
        ArrayList<String> add = new ArrayList<>();
        ArrayList<String> alter = new ArrayList<>();
        ArrayList<String> drop = new ArrayList<>();
        for (ColumnDiff detail : details) {
            switch (detail.actionType) {
                case ADD:
                    add.add(prepareColumnDef(detail.source));
                    break;
                case ALTER:
                    alter.add(prepareColumnDef(detail.source));
                    break;
                case DROP:
                    drop.add(detail.target.getColumnName());
                    break;
            }
        }

        String cmd = "";
        if (!add.isEmpty()) {
            cmd += ("ALTER TABLE \"" + tableName.toUpperCase() + "\"\n ADD(" + String.join(",", add) + ");\n");
        }
        if (!alter.isEmpty()) {
            cmd += ("ALTER TABLE \"" + tableName.toUpperCase() + "\"\n MODIFY(" + String.join(",", alter) + ");\n");
        }
        if (!drop.isEmpty()) {
            cmd += ("ALTER TABLE \"" + tableName.toUpperCase() + "\"\n DROP(" + String.join(",", drop) + ");\n");
        }

        return cmd;
    }

    @Override
    public String generateDropTableSQL(String tableName) {
        return "DROP TABLE " + tableName.toUpperCase();
    }

    @Override
    public String generateDropViewSQL(String viewName) {
        return "DROP VIEW " + viewName.toUpperCase();
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
                    ColumnType ct = new OracleColumnType();
                    ct.setPk(pks.contains(columnName));
                    ct.setColumnName(columnName);
                    ct.setDecimalDigits(rs.getInt("DECIMAL_DIGITS"));
                    ct.setDataTypeCode(rs.getInt("DATA_TYPE"));
                    ct.setDataTypeName(rs.getString("TYPE_NAME"));
                    ct.setNullable("1".equals(rs.getString("NULLABLE")));
                    ct.setColumnSize(rs.getInt("COLUMN_SIZE"));
                    ct.setRemark(rs.getString("REMARKS"));

                    switch (rs.getInt("DATA_TYPE")) {   // ORACLE TYPE
                        case Types.VARCHAR:             // VARCHAR, VARCHAR2
                        case Types.LONGVARCHAR:
                            ct.setDataType(DataType.VARCHAR2);
                            break;
                        case Types.NVARCHAR:            // 
                        case 1111:                      // NVARCHAR2
                            ct.setDataType(DataType.NVARCHAR2);
                            break;
                        case Types.CHAR:                // CHAR
                            ct.setDataType(DataType.NVARCHAR2);
                            break;
                        case Types.SMALLINT:
                        case Types.TINYINT:
                        case Types.INTEGER:             // INTEGER
                            ct.setDataType(DataType.INTEGER);
                            break;
                        case Types.BIGINT:
                            ct.setDataType(DataType.LONG);
                            break;
                        case Types.NUMERIC:
                        case 3:                         // NUMBER
                            if (ct.getDecimalDigits() <= 0 && ct.getColumnSize() < 20) {
                                ct.setDataType(ct.getColumnSize() > 9 ? DataType.LONG : DataType.INTEGER);
                            }
                            else {
                                ct.setDataType(DataType.NUMERIC);
                            }
                            break;
                        case Types.FLOAT:
                            ct.setDataType(DataType.FLOAT);
                            break;
                        case Types.DOUBLE:
                            ct.setDataType(DataType.DOUBLE);
                            break;
                        case Types.TIMESTAMP:           // DATE, TIMESTAMP
                            if (ct.getDataTypeName().startsWith("TIMESTAMP")) {
                                if (isAlwaysTimestampZ()) {
                                    ct.setDataType(DataType.TIMESTAMPZ);
                                }
                                else {
                                    ct.setDataType(DataType.TIMESTAMP);
                                }
                            }
                            else {
                                ct.setDataType(DataType.DATE);
                            }
                            break;
                        case Types.TIME_WITH_TIMEZONE:
                        case -101:                      // TIMESTAMP WITH TIME ZONE
                            ct.setDataType(DataType.TIMESTAMPZ);
                            break;
                        case Types.BLOB:                // BLOB
                            ct.setDataType(DataType.BLOB);
                            break;
                        case Types.CLOB:                // CLOB
                            ct.setDataType(DataType.CLOB);
                            break;
                        case Types.NCLOB:               // NCLOB
                            ct.setDataType(DataType.NCLOB);
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
        switch (ct.getDataType()) {
            case LONG:
                type = "NUMBER(19)";
                break;
            case NUMERIC:
                long cs = ct.getColumnSize();
                if (ct.getDecimalDigits() <= 0) {
                    type = "NUMBER("
                            + (cs >= 38 || cs <= 0 ? 38 : ct.getColumnSize())
                            + ")";
                }
                else {
                    type = "NUMBER("
                            + (cs >= 38 || cs <= 0 ? 38 : ct.getColumnSize())
                            + ","
                            + ct.getDecimalDigits()
                            + ")";
                }
                break;
            case DOUBLE:
            case FLOAT:
                type = "NUMBER(38,6)";
                break;
            case INTEGER:
                type = "INTEGER";
                break;
            case DATE:
                type = "DATE";
                break;
            case TIME:
            case TIMESTAMP:
                type = "TIMESTAMP(6)";
                break;
            case TIMESTAMPZ:
                type = "TIMESTAMP WITH TIME ZONE";
                break;
            case NVARCHAR:
            case NVARCHAR2:
                type = "NVARCHAR2(" + (ct.getColumnSize() == 0 ? 32 : ct.getColumnSize()) + ")";
                break;
            case VARCHAR:
            case VARCHAR2:
                if (isAlwaysNVarchar()) {
                    type = "NVARCHAR2(" + (ct.getColumnSize() == 0 ? 32 : ct.getColumnSize()) + ")";
                }
                else {
                    type = "VARCHAR2(" + (ct.getColumnSize() == 0 ? 32 : ct.getColumnSize()) + ")";
                }
                break;
            case BLOB:
                type = "BLOB";
                break;
            case CLOB:
                type = "CLOB";
                break;
            case JSON:
            case NCLOB:
                type = "NCLOB";
                break;
            default:
                throw new NullPointerException(String.format("%s type not found: %s(%s)",
                        ct.getColumnName(),
                        ct.getDataTypeName(),
                        ct.getDataTypeCode()));

        }

        String nullable = "";
        if (ct.isPk() || !ct.isNullable()) {
            nullable = " NOT NULL";
        }

        return " \"" + ct.getColumnName().toUpperCase() + "\" " + type + nullable;
    }
}
