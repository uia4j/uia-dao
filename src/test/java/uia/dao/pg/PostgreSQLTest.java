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

import java.sql.SQLException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import uia.dao.ComparePlan;
import uia.dao.CompareResult;
import uia.dao.DaoFactoryClassPrinter;
import uia.dao.Database;
import uia.dao.ScriptTool;
import uia.dao.TableType;
import uia.dao.hana.Hana;
import uia.dao.ora.Oracle;

/**
 *
 * @author Kyle K. Lin
 *
 */
public class PostgreSQLTest {

    @Test
    public void testSelect() throws Exception {
        try (Database db = new PostgreSQL("localhost", "5432", "postgres", "postgres", "pgAdmin")) {
            db.selectTableNames().forEach(System.out::println);
            db.selectViewNames().forEach(System.out::println);
        }
    }

    @Test
    public void testSelectTable() throws Exception {
        try (Database db = new PostgreSQL("localhost", "5432", "postgres", "postgres", "pgAdmin")) {
            TableType table = db.selectTable("uia_dto_test", false);
            Assert.assertEquals(7, table.getColumns().size());
            table.getColumns().forEach(System.out::println);
        }
    }

    @Test
    public void testGenerateDTO() throws Exception {
        try (Database db = new PostgreSQL("localhost", "5432", "postgres", "postgres", "pgAdmin")) {
            DaoFactoryClassPrinter p = new DaoFactoryClassPrinter(db, "uia_dto_test");
            System.out.println(p.generateDTO("uia.dao.sample", "Sample2"));
        }
    }

    @Test
    public void testGenerateCreateTableSQL() throws Exception {
        try (Database db = new PostgreSQL("localhost", "5432", "postgres", "postgres", "pgAdmin")) {
            TableType table = db.selectTable("uia_dto_test", false);

            System.out.println("=== PostgreSQL ===");
            System.out.println(db.generateCreateTableSQL(table));

            System.out.println("=== Oracle ===");
            try (Database ora = new Oracle()) {
                System.out.println(ora.generateCreateTableSQL(table));
            }

            System.out.println("=== Hana ===");
            try (Database hana = new Hana()) {
                System.out.println(hana.generateCreateTableSQL(table));
            }
        }
    }

    @Test
    public void testAlter() throws Exception {
        try (Hana hana = fwks()) {
            try (PostgreSQL db1 = new PostgreSQL("localhost", "5432", "pmsdb", "pms", "pms")) {
                try (PostgreSQL db2 = new PostgreSQL("localhost", "5432", "pmsdbv1", "postgres", "pgAdmin")) {
                    new ScriptTool(db1).toAlterScript("d:/temp/pmsdbv2_pg.sql", db2);
                    new ScriptTool(db1).toAlterScript("d:/temp/pmsdbv2_hana.sql", hana);
                }
            }
        }
    }

    @Test
    public void testOne() throws Exception {
        try (PostgreSQL db1 = new PostgreSQL("localhost", "5432", "pmsdb", "pms", "pms")) {
            try (PostgreSQL db2 = new PostgreSQL("localhost", "5432", "pmsdb2", "pms", "pms")) {
                TableType curr = db1.selectTable("ma_schedule_item", false);
                TableType prev = db2.selectTable("ma_schedule_item", false);
                CompareResult cr = curr.sameAs(prev);
                if (!cr.isPassed()) {
                    System.out.println(cr);
                }
            }
        }
    }

    @Test
    public void testAlter2Nvarchar() throws Exception {
        try (Database target = new Hana()) {
            try (Database source = new PostgreSQL("localhost", "5432", "pmsdb", "pms", "pms")) {

                // tables
                List<String> ts = source.selectTableNames();
                StringBuilder scripts = new StringBuilder();
                for (String t : ts) {
                    TableType tableOld = source.selectTable(t, false);
                    TableType tableNew = tableOld.toNvarchar();
                    CompareResult cr = tableNew.sameAs(tableOld, new ComparePlan(true, false, false, false, false));
                    scripts.append("-- ").append(t).append("\n");
                    if (!cr.isPassed()) {
                        scripts.append(target.generateAlterTableSQL(tableOld.getTableName(), cr.getDiff())).append("\n");
                    }
                }

                System.out.println(scripts.toString());
            }
        }
    }

    private Hana fwks() throws SQLException {
        return new Hana("192.168.137.245", "39015", null, "WIP", "Sap12345");
    }
}
