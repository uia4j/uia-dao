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
package uia.dao.sqlite;

import org.junit.Test;

import uia.dao.Database;
import uia.dao.pg.PostgreSQL;
import uia.dao.sqlite.SQLite;

/**
 *
 * @author Kyle K. Lin
 *
 */
public class SQLiteTest {

    @Test
    public void testCreate() throws Exception {
        Database pg = new PostgreSQL("localhost", "5432", "pmsdb", "pms", "pms");

        Database sqlite = new SQLite("test/sqlite1");
        sqlite.createTable(pg.selectTable("part_group_part", false));
        sqlite.createTable(pg.selectTable("lookup", false));
        sqlite.createTable(pg.selectTable("equip", false));
        sqlite.createTable(pg.selectTable("equip_group", false));
        sqlite.createView("view_equip", pg.selectViewScript("view_equip"));
        sqlite.close();

        pg.close();
    }
}
