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
package uia.dao.where;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Kyle K. Lin
 *
 */
public class WhereAnd extends Where {

    private ArrayList<Where> wheres;

    WhereAnd() {
        this.wheres = new ArrayList<>();
    }

    @Override
    public boolean hasConditions() {
        return this.wheres.stream()
                .anyMatch(Where::hasConditions);
    }

    public WhereAnd add(Where where) {
        this.wheres.add(where);
        return this;
    }

    @Override
    public String generate() {
        List<String> ws = this.wheres.stream()
                .filter(Where::hasConditions)
                .map(w -> "(" + w.generate() + ")")
                .collect(Collectors.toList());
        return String.join(" and ", ws);
    }

    @Override
    public int accept(PreparedStatement ps, int index) throws SQLException {
        int i = index;
        for (Where w : this.wheres) {
            i = w.accept(ps, i);
        }
        return i;
    }

}
