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
 * Camel naming tool.
 *
 * @author Kyle K. Lin
 *
 */
public final class CamelNaming {

    private CamelNaming() {
    }

    /**
     * Changes the value to camel naming. First character is lower case.
     *
     * @param value The value.
     * @return Result.
     */
    public static String lower(String value) {
        String[] data = value.split("_");
        StringBuilder b = new StringBuilder();
        b.append(data[0].toLowerCase());
        for (int i = 1; i < data.length; i++) {
            b.append(data[i].substring(0, 1).toUpperCase()).append(data[i].substring(1).toLowerCase());
        }
        return b.toString();
    }

    /**
     * Changes the value to camel naming. First character is upper case.
     *
     * @param value The value.
     * @return Result.
     */
    public static String upper(String value) {
        String[] data = value.split("_");
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            b.append(data[i].substring(0, 1).toUpperCase()).append(data[i].substring(1).toLowerCase());
        }
        return b.toString();
    }
}
