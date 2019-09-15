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

import org.junit.Assert;
import org.junit.Test;

import uia.dao.CompareResult;
import uia.dao.Database;
import uia.dao.TableType;
import uia.dao.sqlite.SQLite;

/**
 *
 * @author Kyle K. Lin
 *
 */
public class DatabaseTest {

    @Test
    public void test1() throws Exception {
        try (Database db = sqlit1()) {
            Assert.assertEquals(4, db.selectTableNames().size());
            Assert.assertEquals(4, db.selectTableNames(null).size());
            Assert.assertEquals(2, db.selectTableNames("equip").size());
            Assert.assertEquals(12, db.selectColumns("equip", false).size());
            Assert.assertNotNull(db.selectTable("equip", false));
            Assert.assertNotNull(db.selectTable("equip_group", false));
            Assert.assertNull(db.selectTable("pms", false));

            Assert.assertEquals(1, db.selectViewNames().size());
            Assert.assertEquals(1, db.selectViewNames(null).size());
            Assert.assertEquals(1, db.selectViewNames("view_").size());

            System.out.println(db.selectViewScript("view_equip"));
        }
    }

    @Test
    public void test2() throws Exception {
        try (Database db1 = sqlit1()) {
            try (Database db2 = sqlit2()) {
                if (db2.exists("equip")) {
                    db2.dropTable("equip");
                }
                if (db2.exists("view_equip")) {
                    db2.dropView("view_equip");
                }

                db2.createTable(db1.selectTable("equip", false));
                Assert.assertEquals(1, db2.selectTableNames().size());
                db2.dropTable("equip");
                Assert.assertEquals(0, db2.selectTableNames().size());

                db2.createView("view_equip", db1.selectViewScript("view_equip"));
                Assert.assertEquals(1, db2.selectViewNames().size());
                db2.dropView("view_equip");
                Assert.assertEquals(0, db2.selectViewNames().size());

            }
        }
    }

    @Test
    public void test3() throws Exception {
        try (Database db1 = sqlit1()) {
            TableType t1 = db1.selectTable("equip", false);
            Assert.assertEquals("DELETE FROM equip WHERE id=?", t1.generateDeleteSQL());

            try (Database db2 = sqlit2()) {
                if (db2.exists("equip")) {
                    db2.dropTable("equip");
                }

                db2.createTable(t1);
                TableType t2 = db2.selectTable("equip", false);
                db2.dropTable("equip");

                CompareResult cr1 = t1.sameAs(t2);
                Assert.assertTrue(cr1.isPassed());

                t2.getColumns().remove(1);
                CompareResult cr2 = t1.sameAs(t2);
                Assert.assertFalse(cr2.isPassed());
                cr2.print(true);
            }
        }
    }

    private Database sqlit1() throws Exception {
        return new SQLite("test/sqlite1");
    }

    private Database sqlit2() throws Exception {
        return new SQLite("test/sqlite2");
    }
}
