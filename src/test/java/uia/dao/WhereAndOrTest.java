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

import uia.dao.where.SimpleWhere;
import uia.dao.where.Where;
import uia.dao.where.WhereAnd;
import uia.dao.where.WhereOr;

/**
 *
 * @author Kyle K. Lin
 *
 */
public class WhereAndOrTest {

    @Test
    public void testOr() {
        Where and1 = Where.simpleAnd().eq("A", "A").eq("B", "B");
        System.out.println("and1: " + and1.generate());
        Where and2 = Where.simpleAnd().eq("C", "C").eq("D", "D");
        System.out.println("and2: " + and2.generate());

        Where where = Where.or(and1, and2);
        System.out.println("(and) or (and): " + where.generate());
        Assert.assertEquals("(A=? and B=?) or (C=? and D=?)", where.generate());
    }

    @Test
    public void testAnd() {
        Where or1 = Where.simpleOr().eq("A", "A").eq("B", "B");
        System.out.println("or1: " + or1.generate());
        Where or2 = Where.simpleOr().eq("C", "C").eq("D", "D");
        System.out.println("or2: " + or2.generate());
        Where and3 = Where.simpleAnd().eq("E", "E").eq("F", "F");
        System.out.println("and3: " + and3.generate());

        Where where1 = Where.and(or1, or2);
        System.out.println("(or) and (or):" + where1.generate());
        Assert.assertEquals("(A=? or B=?) and (C=? or D=?)", where1.generate());

        Where where2 = Where.or(where1, and3);
        System.out.println("(...) or (and):" + where2.generate());
        Assert.assertEquals("((A=? or B=?) and (C=? or D=?)) or (E=? and F=?)", where2.generate());
    }

    @Test
    public void testOrEmpty() {
        WhereOr where = Where.or();
        SimpleWhere and1 = Where.simpleAnd();
        SimpleWhere or2 = Where.simpleOr();

        Assert.assertFalse(where.hasConditions());
        Assert.assertFalse(and1.hasConditions());
        Assert.assertFalse(or2.hasConditions());
        Assert.assertEquals("", where.generate());
        Assert.assertEquals("", and1.generate());
        Assert.assertEquals("", or2.generate());

        where.add(and1);
        Assert.assertFalse(where.hasConditions());

        and1.eq("a", "10");
        Assert.assertTrue(and1.hasConditions());
        Assert.assertTrue(where.hasConditions());
        Assert.assertEquals("(a=?)", where.generate());

        where.add(or2);
        Assert.assertEquals("(a=?)", where.generate());
        or2.eq("b", "20");
        Assert.assertEquals("(a=?) or (b=?)", where.generate());
    }

    @Test
    public void testAndEmpty() {
        WhereAnd where = Where.and();
        SimpleWhere and1 = Where.simpleAnd();
        SimpleWhere or2 = Where.simpleOr();

        Assert.assertFalse(where.hasConditions());
        Assert.assertFalse(and1.hasConditions());
        Assert.assertFalse(or2.hasConditions());
        Assert.assertEquals("", where.generate());
        Assert.assertEquals("", and1.generate());
        Assert.assertEquals("", or2.generate());

        where.add(and1);
        Assert.assertFalse(where.hasConditions());

        and1.eq("a", "10");
        Assert.assertTrue(and1.hasConditions());
        Assert.assertTrue(where.hasConditions());
        Assert.assertEquals("(a=?)", where.generate());

        where.add(or2);
        Assert.assertEquals("(a=?)", where.generate());
        or2.eq("b", "20");
        Assert.assertEquals("(a=?) and (b=?)", where.generate());
    }
}
