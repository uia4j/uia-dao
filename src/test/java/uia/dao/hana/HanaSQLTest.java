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

import java.sql.SQLException;
import java.util.Map;

import org.junit.Test;

import uia.dao.AbstractDatabase.IndexInfo;
import uia.dao.Database;
import uia.dao.TableType;
import uia.dao.ora.Oracle;
import uia.dao.pg.PostgreSQL;

/**
 *
 * @author Kyle K. Lin
 *
 */
public class HanaSQLTest {

    @Test
    public void testSelectTableNames() throws Exception {
        Database db = db();
        db.selectTableNames().forEach(System.out::println);
        db.close();
    }

    @Test
    public void testSelectViewNames() throws Exception {
        Database db = db();
        db.selectViewNames("VIEW_").forEach(System.out::println);
        db.close();
    }

    @Test
    public void testSelectTable() throws Exception {
        Hana db = mes();
        TableType table = db.selectTable("ZR_STDF_UPLOAD", false);
        System.out.println(table.getTableName());
        table.getColumns().forEach(System.out::println);
        System.out.println(table.generateInsertSQL());
        System.out.println(table.generateUpdateSQL());
        System.out.println(table.generateSelectSQL());
        System.out.println(db.generateCreateTableSQL(table));

        db.close();
    }

    @Test
    public void testSelectView() throws Exception {
        Database db = db();
        TableType table = db.selectTable("VIEW_DISPATCH_SFC", false);
        System.out.println(table.getTableName());
        table.getColumns().forEach(System.out::println);
        System.out.println(table.generateSelectSQL());

        db.close();
    }

    @Test
    public void testSelectIndexScripts() throws Exception {
        Hana road = db();
        Hana dsim = dbold();
        road.selectTableNames().forEach(t -> {
            try {
                System.out.println("== " + t + " ==");
                Map<String, IndexInfo> news = road.selectIndexScripts(t);
                Map<String, IndexInfo> olds = dsim.selectIndexScripts(t);
                olds.forEach((k, v) -> {
                    IndexInfo ii = news.get(k);
                    if (ii == null) {
                        System.out.println(v.script());
                    }
                    else if (v.same(ii)) {
                        System.out.println("DROP INDEX " + v.getIndexName() + ";");
                        System.out.println(v.script());
                    }
                });
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        dsim.close();
        road.close();
    }

    @Test
    public void testGenerateCreateTableSQL() throws Exception {
        Database db = db();
        TableType table = db.selectTable("SHOP_ORDER", false);

        System.out.println("=== Hana ===");
        System.out.println(db.generateCreateTableSQL(table));

        System.out.println("=== Oracle ===");
        try (Database ora = new Oracle()) {
            System.out.println(ora.generateCreateTableSQL(table));
        }

        System.out.println("=== PogtgreSQL ===");
        try (PostgreSQL pg = new PostgreSQL()) {
            System.out.println(pg.generateCreateTableSQL(table));
        }

        db.close();
    }

    @Test
    public void testVeiwScript() throws Exception {
        Database db = mes();
        System.out.println(db.selectViewScript("VIEW_DISPATCH_SFC"));
        db.close();
    }

    @Test
    public void testTriggerScripts() throws Exception {
        Database db = mes();
        db.selectTriggerScripts("resrce").forEach(s -> {
            System.out.println("=======");
            System.out.println(s);
        });
        db.close();
    }

    private Hana db() throws SQLException {
        return new Hana("10.160.2.38", "30015", "ROAD", "ROAD", "Road12345");
    }

    private Hana dbold() throws SQLException {
        return new Hana("10.160.2.20", "30015", "DSIM", "DSIM", "Dsim12345");
    }

    private Hana mes() throws SQLException {
        return new Hana("10.160.2.20", "30015", "WIP", "WIP", "Sap12345");
    }

}
