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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import uia.dao.sample.One;
import uia.dao.sample.Two;
import uia.dao.sample.ViewOne;

public class DaoFactoryTest {

    @Test
    public void testDate() {
        Date now1 = new Date();
        LocalDateTime z = now1.toInstant().atZone(ZoneId.of("Z")).toLocalDateTime().minusHours(8);
        Date now2 = Date.from(z.atZone(ZoneId.of("Z")).toInstant());
        System.out.println(now1);
        System.out.println(now2);
    }

    @Test
    public void testOne() throws Exception {
        DaoFactory factory = new DaoFactory(false);
        factory.load("uia.dao.sample");

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
        factory.load("uia.dao.sample");

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
        factory.load("uia.dao.sample");

        ViewDaoHelper<ViewOne> dao = factory.forView(ViewOne.class);
        Assert.assertEquals("SELECT description,id,name,birthday FROM view_one ", dao.forSelect().getSql());
    }
}
