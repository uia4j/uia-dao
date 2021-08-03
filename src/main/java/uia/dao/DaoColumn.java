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

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author Kyle K. Lin
 *
 */
public class DaoColumn {

    private final Field field;

    private DaoColumnWriter writer;

    private DaoColumnReader reader;

    public DaoColumn(Field field, DaoColumnReader reader, DaoColumnWriter writer) {
        this.field = field;
        this.field.setAccessible(true);
        this.reader = reader;
        this.writer = writer;
    }

    void run(Object obj, PreparedStatement ps, int index) throws SQLException, DaoException {
        try {
            this.writer.write(ps, index, this.field.get(obj));
        }
        catch (Exception e) {
            throw new DaoException(String.format("%s(%s) write failed", this, index), e);
        }
    }

    void run(Object obj, ResultSet rs, int index) throws SQLException, DaoException {
        try {
            this.field.set(obj, this.reader.read(rs, index));
        }
        catch (Exception e) {
            throw new DaoException(String.format("%s(%s) read failed", this, index), e);
        }
    }

    @Override
    public String toString() {
        return this.field.getName();
    }

}
