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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import uia.dao.Database;
import uia.dao.SelectStatement;
import uia.dao.sqlite.SQLite;
import uia.dao.where.SimpleWhere;
import uia.dao.where.Where;

/**
 *
 * @author Kyle K. Lin
 *
 */
public class SelectStatementTest {

    @Test
    public void test1() throws Exception {
        SimpleWhere where = Where.simpleAnd()
                .eq("state_name", "on")
                .moreThan("ma_count", 0, false)
                .eqOrNull("equip_group_id", null);

        try (Database db = createDB()) {
            SelectStatement select = new SelectStatement("SELECT id,equip_group_id,state_name,sub_state_name FROM equip")
                    .where(where)
                    .orderBy("id");
            try (PreparedStatement ps = select.prepare(db.getConnection())) {
                try (ResultSet rs = ps.executeQuery()) {
                    Assert.assertFalse(rs.next());
                }
            }
        }
    }

    @Test
    public void test2() throws Exception {
        SimpleWhere where = Where.simpleAnd()
                .notEq("state_name", "on")
                .lessThan("ma_count", 100, true)
                .like("equip_group_id", "e");

        try (Database db = createDB()) {
            SelectStatement select = new SelectStatement("SELECT equip_group_id FROM equip")
                    .where(where)
                    .groupBy("equip_group_id");
            try (PreparedStatement ps = select.prepare(db.getConnection())) {
                try (ResultSet rs = ps.executeQuery()) {
                    Assert.assertFalse(rs.next());
                }
            }
        }
    }

    @Test
    public void test3() throws Exception {
        try (Database db = createDB()) {
            SelectStatement select = new SelectStatement("SELECT equip_group_id FROM equip")
                    .groupBy("equip_group_id")
                    .orderBy("equip_group_id", false);
            try (PreparedStatement ps = select.prepare(db.getConnection())) {
                try (ResultSet rs = ps.executeQuery()) {
                    Assert.assertFalse(rs.next());
                }
            }
        }
    }

    @Test
    public void test4() throws Exception {
        SimpleWhere where = Where.simpleAnd()
                .between("updated_time", new Date(), new Date())
                .moreThan("updated_time", new Date(), true)
                .lessThan("updated_time", new Date(), false);

        try (Database db = createDB()) {
            SelectStatement select = new SelectStatement("SELECT id,equip_group_id,state_name,sub_state_name FROM equip")
                    .where(where)
                    .orderBy("id");
            try (PreparedStatement ps = select.prepare(db.getConnection())) {
                try (ResultSet rs = ps.executeQuery()) {
                    Assert.assertFalse(rs.next());
                }
            }
        }
    }

    @Test
    public void test5() throws Exception {
        SimpleWhere w1 = Where.simpleAnd().eq("state_name", "on").lessThan("ma_count", 10, false);
        SimpleWhere w2 = Where.simpleAnd().eq("state_name", "edit");
        Where where = Where.or(w1, w2);
        Assert.assertEquals("(state_name=? and ma_count<?) or (state_name=?)", where.generate());

        try (Database db = createDB()) {
            SelectStatement select = new SelectStatement("SELECT id,equip_group_id,state_name,sub_state_name FROM equip")
                    .where(where)
                    .orderBy("id");
            try (PreparedStatement ps = select.prepare(db.getConnection())) {
                try (ResultSet rs = ps.executeQuery()) {
                    Assert.assertFalse(rs.next());
                }
            }
        }
    }

    @Test
    public void test6() throws Exception {
        SimpleWhere w1 = Where.simpleOr().eq("state_name", "on").eq("state_name", "edit");
        SimpleWhere w2 = Where.simpleOr().moreThan("ma_count", 10, true);
        Where where = Where.and(w1, w2);
        Assert.assertEquals("(state_name=? or state_name=?) and (ma_count>=?)", where.generate());

        try (Database db = createDB()) {
            SelectStatement select = new SelectStatement("SELECT id,equip_group_id,state_name,sub_state_name FROM equip")
                    .where(where)
                    .orderBy("id");
            try (PreparedStatement ps = select.prepare(db.getConnection())) {
                try (ResultSet rs = ps.executeQuery()) {
                    Assert.assertFalse(rs.next());
                }
            }
        }
    }

    private Database createDB() throws SQLException {
        return new SQLite("test/sqlite1");
    }
}
