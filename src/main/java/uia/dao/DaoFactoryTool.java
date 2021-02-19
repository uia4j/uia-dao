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
 * DAO Factory tool.
 *
 * @author Kyle K. Lin
 *
 */
public class DaoFactoryTool {

    private final Database source;

    /**
     * Constructor.
     *
     * @param source The source database.
     */
    public DaoFactoryTool(Database source) {
        this.source = source;
    }

    /**
     * Saves all tables to Java DTO files.
     *
     * @param dir The source folder.
     * @param packageName The package.
     * @throws SQLException Failed to execute SQL statements.
     * @throws IOException Failed to save files.
     */
    public void tables2DTO(String dir, String packageName) throws SQLException, IOException {
        for (String t : this.source.selectTableNames()) {
            toDTO(dir, packageName, t);
        }
    }

    /**
     * Saves all views to Java DTO files.
     *
     * @param dir The source folder.
     * @param packageName The package.
     * @throws SQLException Failed to execute SQL statements.
     * @throws IOException Failed to save files.
     */
    public void views2DTO(String dir, String packageName) throws SQLException, IOException {
        for (String v : this.source.selectViewNames()) {
            toDTO(dir, packageName, v);
        }
    }

    /**
      * Saves a table or a view to Java DTO files.
    *
     * @param dir The source folder.
     * @param packageName The package.
     * @param tableOrView The table or view name.
     * @throws SQLException Failed to execute SQL statements.
     * @throws IOException Failed to save files.
     */
    public void toDTO(String dir, String packageName, String tableOrView) throws IOException, SQLException {
        String dtoName = CamelNaming.upper(tableOrView);
        DaoFactoryClassPrinter printer = new DaoFactoryClassPrinter(this.source, tableOrView);
        String cls = printer.generateDTO(packageName, dtoName);
        StringBuilder path = new StringBuilder(dir)
	            .append("/")
	            .append(packageName.replace('.', '/'))
	            .append("/");
        Files.createDirectories(Paths.get(path.toString()));
        String file = path.append(dtoName).append(".java").toString();
        Files.write(Paths.get(file), cls.getBytes());
    }
}
