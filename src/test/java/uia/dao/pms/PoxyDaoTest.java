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
package uia.dao.pms;

import java.util.List;
import java.util.UUID;

import org.junit.Test;

import uia.dao.DaoFactory;
import uia.dao.Database;
import uia.dao.pg.PostgreSQL;
import uia.dao.pms.Lookup;
import uia.dao.pms.LookupDao;
import uia.dao.pms.ViewEquip;
import uia.dao.pms.ViewEquipDao;

public class PoxyDaoTest {

    @Test
    public void testLoad() throws Exception {
        DaoFactory factory = new DaoFactory(true);
        factory.load("uia.dao.pms");
        factory.getTables().forEach(System.out::println);
        factory.getViews().forEach(System.out::println);
    }
	
	@Test
	public void testTableProxy() throws Exception {
		DaoFactory factory = new DaoFactory(true);
		factory.addTable(Lookup.class);
		
		try(Database db = new PostgreSQL("localhost", "5432", "pmsdb", "pms", "pms")) {
			LookupDao dao = factory.proxyTableDao(
					LookupDao.class, 
					db.getConnection());
			System.out.println(dao);
			
			List<Lookup> result = dao.select("system"); 
			for(Lookup lk : result) {
				System.out.println(lk);
			}
			
			Lookup lk = new Lookup();
			lk.setId("123");
			lk.setSubId(UUID.randomUUID().toString());
			lk.setParamName("123");
			lk.setParamValue("123");
			dao.insert(lk);
			System.out.println(dao.update("123", "123"));
			System.out.println(dao.delete("123"));
		}
	}
	
	@Test
	public void testViewProxy() throws Exception {
		DaoFactory factory = new DaoFactory(true);
		factory.addView(ViewEquip.class);
		
		try(Database db = new PostgreSQL("localhost", "5432", "pmsdb", "pms", "pms")) {
			ViewEquipDao dao = factory.proxyViewDao(
					ViewEquipDao.class, 
					db.getConnection());
			
			List<ViewEquip> result1 = dao.selectWithGroup(); 
			for(ViewEquip eq : result1) {
				System.out.println(eq.getId() + ":" + eq.getEquipGroupId());
			}
			System.out.println();
			List<ViewEquip> result2 = dao.selectByGroup("ACTE");
			for(ViewEquip eq : result2) {
				System.out.println(eq.getId() + ":" + eq.getEquipGroupId());
			}
		}
		
	}
}
