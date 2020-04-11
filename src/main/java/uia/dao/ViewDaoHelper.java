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
import java.util.ArrayList;

import uia.dao.annotation.ColumnInfo;
import uia.dao.annotation.ViewInfo;

/**
 * The helper for the ViewDao.
 *
 * @author Kyle K. Lin
 *
 */
public final class ViewDaoHelper<T> {

    private final String viewClassName;

    private final String viewName;

    private final DaoMethod<T> select;

    private final String orderBy;

    private final String code;

    ViewDaoHelper(DaoFactory factory, Class<T> clz) {
        ViewInfo ti = clz.getDeclaredAnnotation(ViewInfo.class);
        if (ti == null) {
            throw new NullPointerException(clz.getName() + ": @ViewInfo annotation not found");
        }

        this.viewClassName = clz.getName();
        this.viewName = factory.readSchema(ti.schema()) + ti.name();
        this.select = new DaoMethod<>(clz);
        this.orderBy = ti.orderBy().trim().isEmpty() ? "" : ti.orderBy();
        this.code = ti.code();

        ArrayList<String> selectColNames = new ArrayList<>();
        Class<?> curr = clz;
        for (int i = 0; i <= ti.inherit(); i++) {
            Field[] fs = curr.getDeclaredFields();

            for (Field f : fs) {
                ColumnInfo ci = f.getDeclaredAnnotation(ColumnInfo.class);
                if (ci != null && ci.inView()) {
                    String typeName = ci.typeName();
                    if (typeName.isEmpty()) {
                        typeName = f.getType().getSimpleName();
                    }

                    DaoColumn column = new DaoColumn(
                            f,
                            factory.getColumnReader(typeName),
                            factory.getColumnWriter(typeName));

                    this.select.addColumn(column);
                    selectColNames.add(ci.name());

                }
            }
            curr = curr.getSuperclass();
        }
        this.select.setSql(String.format("SELECT %s FROM %s ",
                String.join(",", selectColNames),
                this.viewName));
    }

    /**
     * Returns the class name of the view..
     *
     * @return The class name.
     */
    public String getViewClassName() {
        return this.viewClassName;
    }

    /**
     * Returns the view name.
     *
     * @return The view name.
     */
    public String getViewName() {
        return this.viewName;
    }

    /**
     * Returns the code of this view.
     * @return The code.
     */
    public String getCode() {
        return this.code;
    }

    /**
     * Returns a method for SELECT which contains all columns of the view.<br>
     * The SQL will be 'SELECT c1,c2... FROM view_name'.
     *
     * @return The SELECT method.
     */
    public DaoMethod<T> forSelect() {
        return this.select;
    }

    public String getOrderBy() {
        return this.orderBy;
    }

    @Override
    public String toString() {
        return this.viewName;
    }
}
