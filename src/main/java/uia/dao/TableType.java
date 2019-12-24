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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The structure of a table.
 *
 * @author Kyle K. Lin
 *
 */
public class TableType {

    private final String tableName;

    private final String remark;

    private final List<ColumnType> columns;

    private final boolean table;

    /**
     * Constructor.
     *
     * @param tableName The table name.
     * @param remark Table comment.
     * @param columns Definition of columns.
     * @param table Is table or not.
     */
    public TableType(String tableName, String remark, List<ColumnType> columns, boolean table) {
        this.tableName = tableName;
        this.remark = remark;
        this.columns = columns;
        this.table = table;
    }

    /**
     * Returns table name.
     *
     * @return Table name.
     */
    public String getTableName() {
        return this.tableName;
    }

    /**
     * Returns table comment.
     *
     * @return Table comment.
     */
    public String getRemark() {
        return this.remark;
    }

    /**
     * Tests if this is table or view.
     *
     * @return True if this is a table.
     */
    public boolean isTable() {
        return this.table;
    }

    /**
     * Selects columns of primary key.
     *
     * @return Columns.
     */
    public List<ColumnType> selectPk() {
        return this.columns.stream()
                .filter(ColumnType::isPk)
                .collect(Collectors.toList());
    }

    /**
     * Selects columns of primary key.
     *
     * @return Names of columns.
     */
    public List<String> selectPkNames() {
        return this.columns.stream()
                .filter(ColumnType::isPk)
                .map(c -> c.columnName)
                .collect(Collectors.toList());
    }

    /**
     * Returns column Definition.
     *
     * @return Columns.
     */
    public List<ColumnType> getColumns() {
        return this.columns;
    }

    /**
     * Compares two tables.
     *
     * @param targetTable The table to be compared.
     * @return Result.
     */
    public CompareResult sameAs(TableType targetTable) {
        return sameAs(targetTable, ComparePlan.table());
    }

    /**
     * Compares two tables.
     *
     * @param table The table to be compared.
     * @param plan The plan.
     * @return Result.
     */
    public CompareResult sameAs(TableType table, ComparePlan plan) {
        CompareResult cr = new CompareResult(this.tableName);
        if (table == null || !this.tableName.equalsIgnoreCase(table.getTableName())) {
            cr.setMissing(true);
            cr.setPassed(false);
            cr.addMessage("tableName mismatch");
            return cr;
        }

        try {
            Map<String, ColumnType> sourceColumns = this.columns.stream().collect(Collectors.toMap(c -> c.getColumnName().toUpperCase(), c -> c));
            Map<String, ColumnType> targetColumns = table.columns.stream().collect(Collectors.toMap(c -> c.getColumnName().toUpperCase(), c -> c));
            for (Map.Entry<String, ColumnType> kv : sourceColumns.entrySet()) {
                ColumnType ct1 = kv.getValue();
                ColumnType ct2 = targetColumns.remove(kv.getKey());
                ct1.sameAs(ct2, plan, cr);
            }
            if (targetColumns.size() > 0) {
                cr.setPassed(false);
            }
            for (Map.Entry<String, ColumnType> kv : targetColumns.entrySet()) {
                cr.addMessage(kv.getKey() + " exists in the target database");
                cr.getDiff().add(new ColumnDiff(kv.getValue(), ColumnDiff.ActionType.DROP, null));
            }
        }
        catch (Exception ex) {
            cr.setPassed(false);
            cr.addMessage(ex.getMessage());
        }

        return cr;
    }

    /**
     * Generates SELECT statement for this table.
     *
     * @return The SELECT statement.
     */
    public String generateSelectSQL() {
        ArrayList<String> cs = new ArrayList<>();
        for (ColumnType column : this.columns) {
            cs.add(column.getColumnName().toLowerCase());
        }

        return String.format("SELECT %s FROM %s", String.join(",", cs), this.tableName.toLowerCase());
    }

    /**
     * Generates INSERT statement for this table.
     *
     * @return The INSERT statement.
     */
    public String generateInsertSQL() {
        ArrayList<String> cs = new ArrayList<>();
        ArrayList<String> ps = new ArrayList<>();
        for (ColumnType column : this.columns) {
            cs.add(column.getColumnName().toLowerCase());
            ps.add("?");
        }

        return String.format("INSERT INTO %s(%s) VALUES (%s)",
                this.tableName.toLowerCase(),
                String.join(",", cs),
                String.join(",", ps));
    }

    /**
     * Generates UPDATE statement for this table.
     *
     * @return The SELECT statement.
     */
    public String generateUpdateSQL() {
        ArrayList<String> pks = new ArrayList<>();
        for (ColumnType column : this.columns) {
            if (column.isPk()) {
                pks.add(column.getColumnName().toLowerCase());
            }
        }

        ArrayList<String> cs = new ArrayList<>();
        ArrayList<String> ws = new ArrayList<>();
        for (ColumnType column : this.columns) {
            String columnName = column.getColumnName().toLowerCase();
            if (pks.isEmpty() || pks.contains(columnName)) {
                pks.add(columnName);
                ws.add(columnName + "=?");
            }
            else {
                cs.add(columnName + "=?");
            }
        }

        if (cs.isEmpty()) {
            return null;
        }

        return String.format("UPDATE %s SET %s WHERE %s",
                this.tableName.toLowerCase(),
                String.join(",", cs),
                String.join(" AND ", ws));
    }

    /**
     * Generates DELETE statement for this table.
     *
     * @return The DELETE statement.
     */
    public String generateDeleteSQL() {
        ArrayList<String> pks = new ArrayList<>();
        for (ColumnType column : this.columns) {
            if (column.isPk()) {
                pks.add(column.getColumnName().toLowerCase());
            }
        }

        ArrayList<String> ws = new ArrayList<>();
        for (ColumnType column : this.columns) {
            String columnName = column.getColumnName().toLowerCase();
            if (pks.isEmpty() || pks.contains(columnName)) {
                ws.add(columnName + "=?");
            }
        }

        return String.format("DELETE FROM %s WHERE %s",
                this.tableName.toLowerCase(),
                String.join(" AND ", ws));
    }
}
