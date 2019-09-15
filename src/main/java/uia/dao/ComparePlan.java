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

/**
 *
 * @author Kyle K. Lin
 *
 */
public class ComparePlan {

    public final boolean strictVarchar;

    public final boolean strictNumeric;

    public final boolean strictDateTime;

    public final boolean checkNullable;

    public final boolean checkDataSize;

    public static ComparePlan table() {
        return new ComparePlan(true, true, true, true, true);
    }

    public static ComparePlan view() {
        return new ComparePlan(false, false, false, false, false);
    }

    public ComparePlan(boolean strictVarchar, boolean strictNumeric, boolean strictDateTime, boolean checkNullable, boolean checkDataSize) {
        this.strictVarchar = strictVarchar;
        this.strictNumeric = strictNumeric;
        this.strictDateTime = strictDateTime;
        this.checkNullable = checkNullable;
        this.checkDataSize = checkDataSize;
    }
}
