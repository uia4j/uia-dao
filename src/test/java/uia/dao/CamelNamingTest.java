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

import uia.dao.CamelNaming;

/**
 *
 * @author Kyle K. Lin
 *
 */
public class CamelNamingTest {

    @Test
    public void test() {
        Assert.assertEquals("first", CamelNaming.lower("fIrst"));
        Assert.assertEquals("firstName", CamelNaming.lower("firsT_nAME"));
        Assert.assertEquals("aJob", CamelNaming.lower("a_job"));
        Assert.assertEquals("beAGoodBoy", CamelNaming.lower("be_a_GOOD_boy"));

        Assert.assertEquals("First", CamelNaming.upper("firST"));
        Assert.assertEquals("FirstName", CamelNaming.upper("firsT_NaMe"));
        Assert.assertEquals("AJob", CamelNaming.upper("a_JOB"));
        Assert.assertEquals("BeAGoodBoy", CamelNaming.upper("BE_A_GOOD_BOY"));
    }
}
