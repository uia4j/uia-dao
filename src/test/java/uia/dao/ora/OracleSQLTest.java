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

/**
 *
 * @author Kyle K. Lin
 *
 */
public class OracleSQLTest {

    @Test
    public void testExists() throws Exception {
        try (Oracle db = new Oracle("localhost", "1521", "ORCLCDB.localdomain", "PMS", "PMS")) {
            System.out.println(db.exists("calendar_ex"));
            db.selectTableNames().forEach(System.out::println);
            db.selectViewNames().forEach(System.out::println);
        }
    }
}
