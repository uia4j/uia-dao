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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Kyle K. Lin
 *
 */
public class CompareResult {

    public final String tableName;

    private boolean passed;

    private boolean missing;

    private final List<String> messages;

    public CompareResult(String tableName) {
        this.tableName = tableName;
        this.missing = false;
        this.passed = true;
        this.messages = new ArrayList<>();
    }

    public CompareResult(String tableName, boolean passed, String message) {
        this.tableName = tableName;
        this.missing = false;
        this.passed = passed;
        this.messages = new ArrayList<>();
        this.messages.add(message);
    }

    public boolean isMissing() {
        return this.missing;
    }

    public void setMissing(boolean missing) {
        this.missing = missing;
    }

    public boolean isPassed() {
        return this.passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public void addMessage(String message) {
        this.messages.add(message);
    }

    public List<String> getMessages() {
        return this.messages;
    }

    public void print(boolean printAll) {
        if (printAll || !this.passed) {
            System.out.println(this);
        }
    }

    @Override
    public String toString() {
        String yn = "(?) ";
        if (!this.missing) {
            yn = this.passed ? "(v) " : "(x) ";
        }
        return this.messages.isEmpty()
                ? yn + this.tableName
                : yn + this.tableName + "\n    " + String.join("\n    ", this.messages);
    }
}
