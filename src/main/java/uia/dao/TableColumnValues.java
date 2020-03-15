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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import uia.dao.where.conditions.ConditionType;
import uia.dao.where.conditions.EqType;

/**
 *
 * @author Kyle K. Lin
 *
 */
public class TableColumnValues {

    private final ArrayList<ConditionType> conds;

    public TableColumnValues() {
        this.conds = new ArrayList<>();
    }

    public TableColumnValues put(String key, Object value) {
        if (isEmpty(key)) {
            return this;
        }
        this.conds.add(new EqType(key, value));
        return this;
    }

    public String generate() {
        if (this.conds.isEmpty()) {
            return "";
        }
        List<String> data = this.conds.stream()
                .map(ConditionType::getStatement)
                .collect(Collectors.toList());
        return String.join(",", data);
    }

    public int accept(PreparedStatement ps, int index) throws SQLException {
        int i = index;
        for (ConditionType cond : this.conds) {
            i = cond.accpet(ps, i);
        }
        return i;
    }

    @Override
    public String toString() {
        if (this.conds.isEmpty()) {
            return "";
        }
        List<String> data = this.conds.stream().map(ConditionType::toString).collect(Collectors.toList());
        return String.join(",", data);
    }

    private boolean isEmpty(Object value) {
        return value == null || value.toString().trim().length() == 0;
    }
}
