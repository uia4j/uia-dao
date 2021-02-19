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

import uia.dao.hana.Hana;
import uia.dao.ora.Oracle;
import uia.dao.pg.PostgreSQL;
import uia.dao.sample1.One;
import uia.dao.sample1.Sample1;
import uia.dao.sample1.Sample2;
import uia.dao.sample1.Two;
import uia.dao.sample1.ViewOne;
import uia.dao.sample1.ViewSample;

public class DaoFactoryTest {

    @Test
    public void testLoad() throws Exception {
        DaoFactory factory = new DaoFactory(true);
        factory.load("uia.dao.sample1");
        factory.getTables().forEach(System.out::println);
        factory.getViews().forEach(System.out::println);
    }

    @Test
    public void testTableType() throws Exception {
        DaoFactory factory = new DaoFactory(true);
        factory.addTable(Sample2.class);

        TableType tt = factory.getTableType(Sample1.class);
        System.out.println("-- hana");
        try (Database db = new Hana()) {
            System.out.println(db.generateCreateTableSQL(tt));
        }
        System.out.println();

        System.out.println("-- oracle");
        try (Database db = new Oracle()) {
            System.out.println(db.generateCreateTableSQL(tt));
        }
        System.out.println();

        System.out.println("-- pgsql");
        try (Database db = new PostgreSQL()) {
            System.out.println(db.generateCreateTableSQL(tt));
        }
    }

    @Test
    public void testViewCode() throws Exception {
        DaoFactory factory = new DaoFactory(true);
        factory.addView(ViewSample.class);
        System.out.println(factory.getViewCode(ViewSample.class));
    }

    @Test
    public void testOne() throws Exception {
        DaoFactory factory = new DaoFactory(false);
        factory.load("uia.dao.sample1");

        TableDaoHelper<One> dao = factory.forTable(One.class);
        Assert.assertEquals("INSERT INTO one(id,name,birthday,state_name) VALUES (?,?,?,?)", dao.forInsert().getSql());
        Assert.assertEquals("UPDATE one SET name=?,birthday=?,state_name=? WHERE id=?", dao.forUpdate().getSql());
        Assert.assertEquals("DELETE FROM one ", dao.forDelete().getSql());
        Assert.assertEquals("SELECT id,name,birthday,state_name FROM one ", dao.forSelect().getSql());
        Assert.assertEquals("id=?", dao.forWherePK());
    }

    @Test
    public void testTwo() throws Exception {
        DaoFactory factory = new DaoFactory(false);
        factory.load("uia.dao.sample1");

        TableDaoHelper<Two> dao = factory.forTable(Two.class);
        Assert.assertEquals("INSERT INTO org_supplier(org_id,supplier_id,state_name) VALUES (?,?,?)", dao.forInsert().getSql());
        Assert.assertEquals("UPDATE org_supplier SET state_name=? WHERE org_id=? AND supplier_id=?", dao.forUpdate().getSql());
        Assert.assertEquals("DELETE FROM org_supplier ", dao.forDelete().getSql());
        Assert.assertEquals("SELECT org_id,supplier_id,state_name FROM org_supplier ", dao.forSelect().getSql());
        Assert.assertEquals("org_id=? AND supplier_id=?", dao.forWherePK());
    }

    @Test
    public void testViewOne() throws Exception {
        DaoFactory factory = new DaoFactory(false);
        factory.load("uia.dao.sample1");

        ViewDaoHelper<ViewOne> dao = factory.forView(ViewOne.class);
        Assert.assertEquals("SELECT description,id,name,birthday FROM view_one ", dao.forSelect().getSql());
    }
}
