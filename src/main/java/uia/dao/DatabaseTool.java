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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * Database tool.
 *
 * @author Kyle K. Lin
 *
 */
public class DatabaseTool {

    private final Database source;

    /**
     * Constructor.
     *
     * @param source The source database.
     */
    public DatabaseTool(Database source) {
        this.source = source;
    }

    /**
     * Output a script to generate all tables.
     *
     * @param file The file name.
     * @param tableNames Names of tables.
     * @throws SQLException Failed to execute SQL statements.
     * @throws IOException Failed to save the file.
     */
    public void toTableScript(String file, String... tableNames) throws IOException, SQLException {
        toTableScript(file, this.source, tableNames);
    }

    /**
     * Output a script to generate all tables.
     *
     * @param file The file name.
     * @param target The database the script is executed on.
     * @param tableNames Names of tables.
     * @throws SQLException Failed to execute SQL statements.
     * @throws IOException Failed to save the file.
     */
    public void toTableScript(String file, Database target, String... tableNames) throws IOException, SQLException {
        StringBuilder scripts = new StringBuilder();
        List<String> ts = Arrays.asList(tableNames);
        if (ts.isEmpty()) {
            ts = this.source.selectTableNames();
        }
        for (String t : ts) {
            String sql = target.generateCreateTableSQL(this.source.selectTable(t, false));
            scripts.append(sql).append("\n");
        }
        Files.write(Paths.get(file), scripts.toString().getBytes());
    }

    /**
     * Output a script to generate all view.
     *
     * @param file The file name.
     * @param viewNames Names of views.
     * @throws SQLException Failed to execute SQL statements.
     * @throws IOException Failed to save the file.
     */
    public void toViewScript(String file, String... viewNames) throws IOException, SQLException {
        toViewScript(file, this.source, viewNames);
    }

    /**
     * Output a script to generate all tables.
     *
     * @param file The file name.
     * @param target The database the script is executed on.
     * @param viewNames Names of views.
     * @throws SQLException Failed to execute SQL statements.
     * @throws IOException Failed to save the file.
     */
    public void toDropViewScript(String file, Database target, String... viewNames) throws IOException, SQLException {
        List<String> vs = Arrays.asList(viewNames);
        if (vs.isEmpty()) {
            vs = this.source.selectViewNames();
        }

        StringBuilder scripts = new StringBuilder();
        for (String v : vs) {
            String script = target.generateDropViewSQL(v);
            scripts.append(script).append("\n\n");
        }
        Files.write(Paths.get(file), scripts.toString().getBytes());
    }

    /**
     * Output a script to generate all tables.
     *
     * @param file The file name.
     * @param target The database the script is executed on.
     * @param viewNames Names of views.
     * @throws SQLException Failed to execute SQL statements.
     * @throws IOException Failed to save the file.
     */
    public void toViewScript(String file, Database target, String... viewNames) throws IOException, SQLException {
        List<String> vs = Arrays.asList(viewNames);
        if (vs.isEmpty()) {
            vs = this.source.selectViewNames();
        }

        StringBuilder scripts = new StringBuilder();
        for (String v : vs) {
            String sql = this.source.selectViewScript(v);

            String script = target.generateCreateViewSQL(v, sql);
            scripts.append(script).append("\n\n");
        }
        Files.write(Paths.get(file), scripts.toString().getBytes());
    }

    /**
     * Output a script to alter tables.
     *
     * @param file The file name.
     * @param target Target database.
     * @param tableNames Table names to be checked.
     * @return True if some tables need to be altered.
     * @throws SQLException Failed to execute SQL statements.
     * @throws IOException Failed to save the file.
     */
    public boolean toAlterScript(String file, Database target, String... tableNames) throws SQLException, IOException {
        return toAlterScript(file, target, target, tableNames);
    }

    /**
     * Output a script to alter tables.
     *
     * @param file The file name.
     * @param compareTarget Target database used to compare.
     * @param outputTarget Target database used to output the script file.
     * @param tableNames Table names to be checked.
     * @return True if some tables need to be altered.
     * @throws SQLException Failed to execute SQL statements.
     * @throws IOException Failed to save the file.
     */
    public boolean toAlterScript(String file, Database compareTarget, Database outputTarget, String... tableNames) throws SQLException, IOException {
        StringBuilder scripts = new StringBuilder();
        List<String> ts = tableNames.length == 0 ? this.source.selectTableNames() : Arrays.asList(tableNames);
        boolean alter = false;
        for (String t : ts) {
            TableType tableNew = this.source.selectTable(t, false);
            TableType tableOld = compareTarget.selectTable(t, false);
            CompareResult cr = tableNew.sameAs(tableOld, new ComparePlan(false, false, false, false, true));
            if (!cr.isPassed()) {
                alter = true;
                scripts.append("-- ").append(t).append("\n");
                if (cr.isMissing()) {
                    scripts.append(outputTarget.generateCreateTableSQL(tableNew)).append("\n");
                }
                else {
                    scripts.append(outputTarget.generateAlterTableSQL(tableOld.getTableName(), cr.getDiff())).append("\n");
                }
            }
        }

        if (alter) {
            Files.write(Paths.get(file), scripts.toString().getBytes());
        }
        return alter;
    }
}
