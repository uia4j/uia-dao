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

import org.junit.Test;

import uia.dao.Database;
import uia.dao.TableType;
import uia.dao.hana.Hana;
import uia.dao.ora.Oracle;
import uia.dao.pg.PostgreSQL;

/**
 *
 * @author Kyle K. Lin
 *
 */
public class OracleSQLTest {

    @Test
    public void testExists() throws Exception {
        Database db = new Oracle("10.160.1.48", "1521", "MESDEV", "WIP", "wip");
        System.out.println(db.exists("VIEW_HOLD_SFC"));
        System.out.println(db.exists("VIEW_HOLD_SFC_DETAIL"));
        System.out.println(db.exists("VIEW_HOLD_SF"));
        db.close();
    }

    @Test
    public void testSelectTableNames() throws Exception {
        Database db = new Oracle("10.160.1.48", "1521", "MESDEV", "WIP", "wip");
        db.selectTableNames("ZD_").forEach(t -> System.out.println(t));
        db.close();
    }

    @Test
    public void testSelectViewNames() throws Exception {
        Database db = new Oracle("10.160.1.48", "1521", "MESDEV", "WIP", "wip");
        db.selectViewNames("VIEW_").forEach(t -> System.out.println(t));
        db.close();
    }

    @Test
    public void testSelectTable() throws Exception {
        Database db = new Oracle("10.160.1.48", "1521", "MESDEV", "WIP", "wip");

        TableType table = db.selectTable("VIEW_RESOURCE", true);
        System.out.println(table.getTableName());
        table.getColumns().forEach(System.out::println);
        System.out.println(table.generateInsertSQL());
        System.out.println(table.generateUpdateSQL());
        System.out.println(table.generateSelectSQL());

        db.close();
    }

    @Test
    public void testSelectView() throws Exception {
        Database db = new Oracle("10.160.1.48", "1521", "MESDEV", "WIP", "wip");

        TableType table = db.selectTable("VIEW_DISPATCH_SFC", false);
        System.out.println(table.getTableName());
        table.getColumns().forEach(System.out::println);
        System.out.println(table.generateSelectSQL());

        db.close();
    }

    @Test
    public void testSelectViewScript() throws Exception {
        Database db = new Oracle("10.160.1.48", "1521", "MESDEV", "WIP", "wip");
        System.out.println(db.selectViewScript("VIEW_DISPATCH_SFC"));
        db.close();
    }

    @Test
    public void testGenerateCreateTableSQL() throws Exception {
        Database db = new Oracle("10.160.1.48", "1521", "MESDEV", "WIP", "wip");
        TableType table = db.selectTable("AUTH_GROUP", true);

        System.out.println("=== Oracle ===");
        System.out.println(db.generateCreateTableSQL(table));

        System.out.println("=== Hana ===");
        try (Database hana = new Hana("WIP", null, null, null, null)) {
            System.out.println(hana.generateCreateTableSQL(table));
        }

        System.out.println("=== PogtgreSQL ===");
        try (Database pg = new PostgreSQL("public", null, null, null, null)) {
            System.out.println(pg.generateCreateTableSQL(table));
        }

        db.close();
    }
}
