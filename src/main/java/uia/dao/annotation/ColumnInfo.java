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
package uia.dao.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import uia.dao.ColumnType.DataType;;

@Retention(RetentionPolicy.RUNTIME)
public @interface ColumnInfo {

    String name();

    boolean primaryKey() default false;

    String converter() default "";

    DataType sqlType() default DataType.UNDEFINED;

    boolean inView() default true;

    int length() default 128;

    int scale() default 0;

    String remark() default "";

}
