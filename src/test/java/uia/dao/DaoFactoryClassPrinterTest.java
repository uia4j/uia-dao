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

import java.sql.SQLException;

import org.junit.Test;

import uia.dao.sqlite.SQLite;

/**
 *
 * @author Kyle K. Lin
 *
 */
public class DaoFactoryClassPrinterTest {

    private static final String DTO_PACKAGE = "uia.utils";

    @Test
    public void testGenerateTable() throws Exception {
        Database db = sqlite();
        String tableName = "equip";
        String clz = new DaoFactoryClassPrinter(db, tableName).generateDTO(
                DTO_PACKAGE,
                CamelNaming.upper(tableName));
        System.out.println(clz);
    }

    @Test
    public void testGenerateView() throws Exception {
        Database db = sqlite();
        String viewName = "view_equip";
        String clz = new DaoFactoryClassPrinter(db, viewName).generateDTO(
                DTO_PACKAGE,
                CamelNaming.upper(viewName));
        System.out.println(clz);
    }

    private Database sqlite() throws SQLException {
        return new SQLite("test/sqlite1");
    }

}
