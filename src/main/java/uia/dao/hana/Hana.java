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
package uia.dao.hana;

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
public class Hana extends AbstractDatabase {

    static {
        try {
            Class.forName("com.sap.db.jdbc.Driver");
        }
        catch (Exception e) {

        }
    }

    public Hana() throws SQLException {
        super(null, null, null, null, null);
    }

    public Hana(String host, String port, String schema, String user, String pwd) throws SQLException {
        // jdbc:sap://host:port?reconnect=true
        super("com.sap.db.jdbc.Driver", String.format("jdbc:sap://%s:%s", host, port), user, pwd, schema == null ? user : schema);
    }

    @Override
    public String selectViewScript(String viewName) throws SQLException {
        String script = null;
        try (PreparedStatement ps = this.conn.prepareStatement("SELECT definition FROM VIEWS WHERE schema_name=? AND view_name=?")) {
            ps.setString(1, this.schema);
            ps.setString(2, viewName.toUpperCase());

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
        return String.format("CREATE VIEW \"%s\" AS %n%s", viewName.toUpperCase(), sql);
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
            comments.add(String.format("COMMENT ON TABLE '%s' IS '%s';%n",
                    table.getTableName().toUpperCase(),
                    table.getRemark()));
        }

        for (ColumnType ct : table.getColumns()) {
            if (ct.isPk()) {
                pks.add(ct.getColumnName().toUpperCase());
            }
            cols.add(prepareColumnDef(ct));
            if (ct.getRemark() != null && ct.getRemark().trim().length() > 0) {
                comments.add(String.format("COMMENT ON COLUMN '%s'.'%s' IS '%s';%n",
                        table.getTableName().toUpperCase(),
                        ct.getColumnName().toUpperCase(),
                        ct.getRemark()));
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("CREATE COLUMN TABLE \"" + table.getTableName().toUpperCase() + "\"\n(\n");
        sb.append(String.join(",\n", cols));
        if (pks.isEmpty()) {
            sb.append("\n);\n");
        }
        else {
            sb.append(",\n PRIMARY KEY (\"" + String.join("\",\"", pks) + "\")\n);\n");
        }

        /**
        for (String comment : comments) {
            sb.append(comment);
        }
        */

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
            cmd += ("ALTER TABLE \"" + tableName.toUpperCase() + "\"\n ALTER(" + String.join(",", alter) + ");\n");
        }
        if (!drop.isEmpty()) {
            cmd += ("ALTER TABLE \"" + tableName.toUpperCase() + "\"\n DROP(" + String.join(",", drop) + ");\n");
        }

        return cmd;
    }

    @Override
    public String generateDropTableSQL(String tableName) {
        return "DROP TABLE \"" + tableName.toUpperCase() + "\"";
    }

    @Override
    public String generateDropViewSQL(String viewName) {
        return "DROP VIEW \"" + viewName.toUpperCase() + "\"";
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
                    ColumnType ct = new HanaColumnType();
                    ct.setPk(pks.contains(columnName));
                    ct.setColumnName(columnName);
                    ct.setDecimalDigits(rs.getInt("DECIMAL_DIGITS"));
                    ct.setDataTypeCode(rs.getInt("DATA_TYPE"));
                    ct.setDataTypeName(rs.getString("TYPE_NAME"));
                    ct.setNullable("1".equals(rs.getString("NULLABLE")));
                    ct.setColumnSize(rs.getInt("COLUMN_SIZE"));
                    ct.setRemark(rs.getString("REMARKS"));

                    switch (rs.getInt("DATA_TYPE")) {       // HANA TYPE
                        case Types.VARCHAR:                 // VARCHAR
                            ct.setDataType(DataType.VARCHAR);
                            break;
                        case Types.NVARCHAR:                // NVARCHAR
                            ct.setDataType(DataType.NVARCHAR);
                            break;
                        case Types.FLOAT:                   // FLOAT
                            ct.setDataType(DataType.FLOAT);
                            break;
                        case Types.DOUBLE:                  // DOUBLE
                            ct.setDataType(DataType.DOUBLE);
                            break;
                        case Types.REAL:                    // REAL
                        case Types.DECIMAL:                 // DECIMAL, SMALLDECIMAL
                            ct.setDataType(DataType.NUMERIC);
                            break;
                        case Types.SMALLINT:                // SMALLINT
                        case Types.TINYINT:                 // TINYINT
                        case Types.INTEGER:                 // INTEGER
                            ct.setDataType(DataType.INTEGER);
                            break;
                        case Types.BIGINT:                  // BIGINT
                            ct.setDataType(DataType.LONG);
                            break;
                        case Types.DATE:                    // DATE
                            ct.setDataType(DataType.DATE);
                            break;
                        case Types.TIME:                    // TIME
                        case Types.TIME_WITH_TIMEZONE:
                            ct.setDataType(DataType.TIME);
                            break;
                        case Types.TIMESTAMP:               // SECONDDATE, TIMESTAMP
                        case Types.TIMESTAMP_WITH_TIMEZONE:
                            ct.setDataType(DataType.TIMESTAMP);
                            break;
                        case Types.BLOB:                    // BLOB
                            ct.setDataType(DataType.BLOB);
                            break;
                        case Types.CLOB:                    // CLOB
                            ct.setDataType(DataType.CLOB);
                            break;
                        case Types.NCLOB:                   // NCLOB, TEXT
                            ct.setDataType(DataType.NCLOB);
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
        return value.toUpperCase();
    }

    private String prepareColumnDef(ColumnType ct) {
        String type = "";
        switch (ct.getDataType()) {
            case INTEGER:       // INTEGER, TINYINT, SMALLINT
                type = "INTEGER";
                break;
            case LONG:          // BIGINT
                type = "BIGINT";
                break;
            case NUMERIC:       // DECIMAL, SMALLDECIMAL
                long cs = ct.getColumnSize();
                type = "DECIMAL("
                        + (cs >= 38 || cs <= 0 ? 38 : ct.getColumnSize())
                        + ","
                        + ct.getDecimalDigits()
                        + ")";
                break;
            case FLOAT:
                type = "FLOAT";
                break;
            case DOUBLE:
                type = "DOUBLE";
                break;
            case DATE:          // DATE
                type = "DATE";
                break;
            case TIME:
                type = "TIME";
                break;
            case TIMESTAMP:     // TIMESTEMP
            case TIMESTAMPZ:
                type = "TIMESTAMP";
                break;
            case NVARCHAR:      // NVARCHAR
            case NVARCHAR2:     // NVARCHAR
                type = "NVARCHAR(" + (ct.getColumnSize() == 0 ? 32 : ct.getColumnSize()) + ")";
                break;
            case VARCHAR:       // VARCHAR
            case VARCHAR2:      // VARCHAR
                if (isAlwaysNVarchar()) {
                    type = "NVARCHAR(" + (ct.getColumnSize() == 0 ? 32 : ct.getColumnSize()) + ")";
                }
                else {
                    type = "VARCHAR(" + (ct.getColumnSize() == 0 ? 32 : ct.getColumnSize()) + ")";
                }
                break;
            case BLOB:          // BLOB
                type = "BLOB";
                break;
            case CLOB:          // CLOB
                type = "CLOB";
                break;
            case NCLOB:         // NCLOB
                type = "NCLOB";
                break;
            default:
                throw new NullPointerException(ct.getColumnName() + " type not found:" + ct.getDataTypeName());
        }

        String nullable = "";
        if (ct.isPk()) {
            nullable = " NOT NULL";
        }
        else if (!ct.isNullable()) {
            nullable = " NOT NULL";
        }
        else {
            nullable = " NULL";
        }

        return " \"" + ct.getColumnName().toUpperCase() + "\" " + type + nullable;
    }
}
