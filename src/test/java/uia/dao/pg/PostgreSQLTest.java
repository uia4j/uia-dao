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

import org.junit.Test;

import uia.dao.ColumnType;
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
public class PostgreSQLTest {

    @Test
    public void testSelectTableNames() throws Exception {
        Database db = new PostgreSQL("localhost", "5432", "mvsdb", "huede", "huede");
        db.selectTableNames("ivp").forEach(t -> System.out.println(t));
        db.close();
    }

    @Test
    public void testSelectViewNames() throws Exception {
        Database db = new PostgreSQL("localhost", "5432", "mvsdb", "huede", "huede");
        db.selectViewNames("ivp_").forEach(t -> System.out.println(t));
        db.close();
    }

    @Test
    public void testSelectTable() throws Exception {
        Database db = new PostgreSQL("localhost", "5432", "mvsdb", "huede", "huede");

        print(db.selectTable("hspt", true));
        print(db.selectTable("hspt_area", true));
        print(db.selectTable("ivp_dtu", true));
        print(db.selectTable("ivp_event_def", true));
        print(db.selectTable("ivp", true));
        print(db.selectTable("ivp_agent", true));
        print(db.selectTable("ivp_run", true));
        print(db.selectTable("ivp_raw", true));
        print(db.selectTable("ivp_raw_event", true));
        print(db.selectTable("login_log", true));

        db.close();
    }

    @Test
    public void testPMS() throws Exception {
        final Database db = new PostgreSQL("localhost", "5432", "pmsdb", "pms", "pms");

        db.selectTableNames().forEach(tn -> {
            try {
                TableType t = db.selectTable(tn, true);
                System.out.println(db.generateCreateTableSQL(t));
                print(t);
            }
            catch (Exception e) {

            }
        });

        db.close();
    }

    @Test
    public void testSelectView() throws Exception {
        Database db = new PostgreSQL("localhost", "5432", "mvsdb", "huede", "huede");

        TableType table = db.selectTable("ivp_raw_event_view", false);
        System.out.println(table.getTableName());
        table.getColumns().forEach(System.out::println);
        System.out.println(table.generateInsertSQL());
        System.out.println(table.generateUpdateSQL());
        System.out.println(table.generateSelectSQL());

        db.close();
    }

    @Test
    public void testSelectViewScript() throws Exception {
        Database db = new PostgreSQL("localhost", "5432", "mvsdb", "huede", "huede");
        System.out.println(db.selectViewScript("ivp_raw_event_view"));
        db.close();
    }

    @Test
    public void testGenerateCreateTableSQL() throws Exception {
        Database db = new PostgreSQL("localhost", "5432", "mvsdb", "huede", "huede");
        TableType table = db.selectTable("ivp", false);

        System.out.println("=== PostgreSQL ===");
        System.out.println(db.generateCreateTableSQL(table));

        System.out.println("=== Oracle ===");
        try (Database ora = new Oracle("WIP", null, null, null, null)) {
            System.out.println(ora.generateCreateTableSQL(table));
        }

        System.out.println("=== Hana ===");
        try (Database hana = new Hana("WIP", null, null, null, null)) {
            System.out.println(hana.generateCreateTableSQL(table));
        }

        db.close();
    }

    private void print(TableType table) {
        System.out.println(table.getTableName() + " " + table.getRemark());
        for (ColumnType ct : table.getColumns()) {
            System.out.println(String.format("  %s(%s): %s%s",
                    ct.getColumnName(),
                    ct.getDataTypeName(),
                    ct.getRemark(),
                    ct.isPk() ? ", PK" : ""));
        }
        System.out.println();
    }
}
