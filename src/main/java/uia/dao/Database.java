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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author Kyle K. Lin
 *
 */
public interface Database extends AutoCloseable {

    /**
     * Returns schema name.
     *
     * @return The schema name.
     */
    public String getSchema();

    /**
     * Sets schema name.
     *
     * @param schema The schema name.
     */
    public void setSchema(String schema);

    /**
     * Returns the connection.
     *
     * @return The connection.
     */
    public Connection getConnection();

    /**
     * Returns the connection from connection pool.
     *
     * @return The connection.
     * @throws SQLException Failed to execute.
     */
    public Connection getConnectionFromPool() throws SQLException;

    /**
     * Returns table names.
     *
     * @return Table names.
     * @throws SQLException Failed to execute.
     */
    public List<String> selectTableNames() throws SQLException;

    /**
     * Returns table names.
     *
     * @param prefix Table name prefix.
     * @return Table names.
     * @throws SQLException Failed to execute.
     */
    public List<String> selectTableNames(String prefix) throws SQLException;

    /**
     * Return all table names.
     *
     * @return Table names.
     * @throws SQLException Failed to execute.
     */
    public List<String> selectViewNames() throws SQLException;

    /**
     * Returns view names.
     *
     * @param prefix View name prefix.
     * @return View names.
     * @throws SQLException Failed to execute.
     */
    public List<String> selectViewNames(String prefix) throws SQLException;

    /**
     * Test is table or view exists.
     *
     * @param tableOrView Table name or view name.
     * @return Result.
     * @throws SQLException Failed to execute.
     */
    public boolean exists(String tableOrView) throws SQLException;

    /**
     * Return table or view model.
     *
     * @param tableOrView Table name or view name.
     * @param firstAsPK Change first column to be primary key.
     * @return Table model.
      * @throws SQLException Failed to execute.
    */
    public TableType selectTable(String tableOrView, boolean firstAsPK) throws SQLException;

    /**
     * Return column model.
     *
     * @param tableName Table name.
     * @param firstAsPK Change first column to be primary key.
     * @return Column model.
     * @throws SQLException Failed to execute.
     */
    public List<ColumnType> selectColumns(String tableName, boolean firstAsPK) throws SQLException;

    /**
     * Return view script.
     *
     * @param viewName View name.
     * @return Script.
     * @throws SQLException Failed to execute.
     */
    public String selectViewScript(String viewName) throws SQLException;

    /**
     * Generate script to create a view.
     *
     * @param viewName View name.
     * @param sql View statement.
     * @return Create script.
     */
    public String generateCreateViewSQL(String viewName, String sql);

    /**
     * Generate script to create a table.
     *
     * @param table Table model.
     * @return Create script.
     */
    public String generateCreateTableSQL(TableType table);

    /**
     * Generate script to alter a table.
     *
     * @param tableName Table name.
     * @param cols Column model.
     * @return Update script.
     */
    public String generateAlterTableSQL(String tableName, List<ColumnType> cols);

    /**
     * Create a table.
     *
     * @param table Table model.
     * @return Result.
     * @throws SQLException Failed to execute.
     */
    public int createTable(TableType table) throws SQLException;

    /**
     * Alter columns.
     *
     * @param tableName Table name.
     * @param columns Column model.
     * @return Result.
     * @throws SQLException Failed to execute.
     */
    public int alterTableColumns(String tableName, List<ColumnType> columns) throws SQLException;

    /**
     * Drop a table.
     *
     * @param tableName Table name.
     * @return Result.
     * @throws SQLException Failed to execute.
     */
    public int dropTable(String tableName) throws SQLException;

    /**
     * Create a view.
     *
     * @param viewName View name.
     * @param sql View script.
     * @return Result.
     * @throws SQLException Failed to execute.
     */
    public int createView(String viewName, String sql) throws SQLException;

    /**
     * Drop a view.
     *
     * @param viewName View name.
     * @return Result.
     * @throws SQLException Failed to execute.
     */
    public int dropView(String viewName) throws SQLException;

    /**
     * Execute SQL commands.
     *
     * @param sqls SQL commands.
     * @return Result.
     * @throws SQLException Failed to execute.
     */
    public int[] executeBatch(List<String> sqls) throws SQLException;

    /**
     * Execute SQL commands.
     *
     * @param sql SQL command.
     * @param rows Data.
     * @return Result.
     * @throws SQLException Failed to execute.
     */
    public int[] executeBatch(String sql, List<List<Object>> rows) throws SQLException;

}
