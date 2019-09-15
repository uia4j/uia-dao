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
     * @throws SQLException Failed to execute SQL statements.
     * @throws IOException Failed to save files.
     */
    public void toTableScript(String file) throws IOException, SQLException {
        toTableScript(file, this.source);
    }

    /**
     * Output a script to generate all tables.
     *
     * @param file The file name.
     * @param target The database the script is executed on.
     * @throws SQLException Failed to execute SQL statements.
     * @throws IOException Failed to save files.
     */
    public void toTableScript(String file, Database target) throws IOException, SQLException {
        StringBuilder scripts = new StringBuilder();
        for (String t : this.source.selectTableNames()) {
            String sql1 = target.generateCreateTableSQL(this.source.selectTable(t, false));
            scripts.append(sql1).append(";\n\n");
        }
        Files.write(Paths.get(file), scripts.toString().getBytes());
    }

    /**
     * Output a script to generate all view.
     *
     * @param file The file name.
     * @throws SQLException Failed to execute SQL statements.
     * @throws IOException Failed to save files.
     */
    public void toViewScript(String file) throws IOException, SQLException {
        toViewScript(file, this.source);
    }

    /**
     * Output a script to generate all tables.
     *
     * @param file The file name.
     * @param target The database the script is executed on.
     * @throws SQLException Failed to execute SQL statements.
     * @throws IOException Failed to save files.
     */
    public void toViewScript(String file, Database target) throws IOException, SQLException {
        StringBuilder scripts = new StringBuilder();
        for (String v : this.source.selectViewNames()) {
            String sql = this.source.selectViewScript(v);
            String script = target.generateCreateViewSQL(v, sql);
            scripts.append(script).append(";\n\n");
        }
        Files.write(Paths.get(file), scripts.toString().getBytes());
    }
}
