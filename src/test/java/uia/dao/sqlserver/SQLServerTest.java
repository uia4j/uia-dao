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

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import org.junit.Test;

import uia.dao.DaoFactoryTool;
import uia.dao.Database;
import uia.dao.pg.PostgreSQL;

/**
 *
 * @author Kyle K. Lin
 *
 */
public class SQLServerTest {

    @Test
    public void testExists() throws Exception {
        try (Database db = new SQLServerOld("10.0.3.3", "1433", "Logistic", "redqiao", "123")) {
            db.selectTableNames().forEach(System.out::println);
        }
    }

    @Test
    public void testGenerateTable() throws Exception {
        try (Database db = new SQLServerOld("10.0.3.3", "1433", "Logistic", "redqiao", "123")) {
            DaoFactoryTool tool = new DaoFactoryTool(db);
            List<String> ts = db.selectTableNames();
            for (String t : ts) {
                try {
                    tool.toDTO("D:/workspace/htks/mms/03.code/mms-mes-db/src/main/java", "mms.mes.db", t);
                    System.out.println(t);
                }
                catch (Exception ex) {
                    System.out.println(t + "failed, " + ex.getMessage());
                }
            }
        }
    }

    @Test
    public void testGenerateScripts() throws Exception {
        try (PostgreSQL pg = new PostgreSQL()) {
            try (SQLServerOld db = new SQLServerOld("10.0.3.3", "1433", "Logistic", "redqiao", "123")) {
                StringBuilder sqls = new StringBuilder();
                for (String tn : db.selectTableNames()) {
                    try {
                        String script = pg.generateCreateTableSQL(db.selectTable(tn, true));
                        sqls.append(script);
                        System.out.println(tn);
                    }
                    catch (Exception ex) {
                        System.out.println(tn + "failed, " + ex.getMessage());
                    }
                }
                Files.write(Paths.get("d:/temp/mms_pg2.sql"), sqls.toString().getBytes(), StandardOpenOption.CREATE);
            }
        }
    }
}
