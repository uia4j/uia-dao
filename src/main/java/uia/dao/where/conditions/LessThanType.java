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
package uia.dao.where.conditions;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 *
 * @author Kyle K. Lin
 *
 */
public class LessThanType implements ConditionType {

    private final String key;

    private final Object value;

    private final boolean eq;

    public LessThanType(String key, Object value, boolean eq) {
        this.key = key;
        this.value = value;
        this.eq = eq;
    }

    @Override
    public String getStatement() {
        return this.eq ? this.key + "<=?" : this.key + "<?";
    }

    @Override
    public int accpet(final PreparedStatement ps, final int index) throws SQLException {
    	ConditionType.apply(ps, index, this.value);
        return index + 1;
    }

    @Override
    public String toString() {
        return this.value == null ? this.key + " is null" : this.key + "='" + this.value + "'";
    }
}
