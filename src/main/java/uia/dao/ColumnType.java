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

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

/**
 * The structure of a column.
 *
 * @author Kyle K. Lin
 *
 */
public class ColumnType {

    public static enum DataType {

        /**
         * boolean
         */
        BOOLEAN(Types.BOOLEAN),

        /**
         * varchar
         */
        VARCHAR(Types.VARCHAR),

        /**
         * nvarchar
         */
        NVARCHAR(Types.NVARCHAR),

        /**
         * varchar2
         */
        VARCHAR2(Types.VARCHAR),

        /**
         * nvarchar2
         */
        NVARCHAR2(Types.NVARCHAR),

        /**
         * integer
         */
        INTEGER(Types.INTEGER),

        /**
         * long
         */
        LONG(Types.BIGINT),

        /**
         * numeroc
         */
        NUMERIC(Types.NUMERIC),

        /**
         * float
         */
        FLOAT(Types.FLOAT),

        /**
         * double
         */
        DOUBLE(Types.DOUBLE),

        /**
         * timestamp
         */
        TIMESTAMP(Types.TIMESTAMP),

        /**
         * timestamp with time zone
         */
        TIMESTAMPZ(Types.TIMESTAMP_WITH_TIMEZONE),

        /**
         * date
         */
        DATE(Types.DATE),

        /**
         * time
         */
        TIME(Types.TIME),

        /**
         * blob
         */
        BLOB(Types.BLOB),

        /**
         * clob
         */
        CLOB(Types.CLOB),

        /**
         * nclob
         */
        NCLOB(Types.NCLOB),

        /**
         * json
         */
        JSON(Types.OTHER),

        /**
         * others
         */
        UNDEFINED(Types.NVARCHAR);

        public final int sqlType;

        DataType(int sqlType) {
            this.sqlType = sqlType;
        }
    }

    protected boolean pk;

    protected String columnName;

    protected DataType dataType;

    protected int dataTypeCode;

    protected String dataTypeName;

    protected long columnSize;

    protected boolean nullable;

    protected int decimalDigits;

    protected String remark;

    protected Object defaultValue;

    private final ArrayList<ColumnType.Compare> cs;

    public ColumnType() {
        this.cs = new ArrayList<>();
        this.cs.add(this::checkNullable);
        this.cs.add(this::checkPK);
        this.cs.add(this::checkString);
        this.cs.add(this::checkNumber);
        this.cs.add(this::checkTimestamp);
    }

    /**
     * Tests if this column is a primary key.
     *
     * @return True if the column is a primary key.
     */
    public boolean isPk() {
        return this.pk;
    }

    /**
     * Sets the column to be a primary key or not.
     * @param pk True if the column is a primary key.
     */
    public void setPk(boolean pk) {
        this.pk = pk;
    }

    /**
     * Returns the column name.
     *
     * @return The column name.
     */
    public String getColumnName() {
        return this.columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public DataType getDataType() {
        return this.dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public int getDataTypeCode() {
        return this.dataTypeCode;
    }

    public void setDataTypeCode(int dataTypeCode) {
        this.dataTypeCode = dataTypeCode;
    }

    public String getDataTypeName() {
        return this.dataTypeName;
    }

    public void setDataTypeName(String dataTypeName) {
        this.dataTypeName = dataTypeName;
    }

    public boolean isNullable() {
        return this.nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public long getColumnSize() {
        return this.columnSize;
    }

    public void setColumnSize(long columnSize) {
        this.columnSize = columnSize;
    }

    public int getDecimalDigits() {
        return this.decimalDigits;
    }

    public void setDecimalDigits(int decimalDigits) {
        this.decimalDigits = decimalDigits;
    }

    public String getRemark() {
        return this.remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Object getDefaultValue() {
        return this.defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Tests if this column is string type, including NVARCHAR, NVARCHAR2, VARCHAR, VARCHAR2.
     *
     * @return Result.
     */
    public boolean isStringType() {
        // THINK: include BLOB?
        switch (this.dataType) {
            case NVARCHAR:
            case NVARCHAR2:
            case VARCHAR:
            case VARCHAR2:
                return true;
            default:
                return false;
        }
    }

    /**
     * Tests if this column is date-time, including DATE, TIME, TIMESTAMP.
     *
     * @return Result.
     */
    public boolean isDateTimeType() {
        switch (this.dataType) {
            case DATE:
            case TIME:
            case TIMESTAMP:
                return true;
            default:
                return false;
        }
    }

    /**
     * Tests if this column is numeric including INTEGER, LONG, NUMERIC, FLOAT, DOUBLE.
     *
     * @return Result.
     */
    public boolean isNumericType() {
        switch (this.dataType) {
            case INTEGER:
            case LONG:
            case NUMERIC:
            case FLOAT:
            case DOUBLE:
                return true;
            default:
                return false;
        }
    }

    /**
     * Tests if this column is numeric including INTEGER, LONG, NUMERIC, FLOAT, DOUBLE.
     *
     * @return Result.
     */
    public boolean isInteger() {
        switch (this.dataType) {
            case INTEGER:
            case LONG:
                return true;
            default:
                return false;
        }
    }

    /**
     * Returns the Java type for this column.
     *
     * @return Type name of Java.
     */
    public String getJavaTypeName() {
        switch (this.dataType) {
            case BOOLEAN:
                return this.nullable ? "Boolean" : "boolean";
            case INTEGER:
                return this.nullable ? "Integer" : "int";
            case LONG:
                return this.nullable ? "Long" : "long";
            case NUMERIC:
            case FLOAT:
            case DOUBLE:
                return "BigDecimal";
            case DATE:
            case TIME:
            case TIMESTAMP:
            case TIMESTAMPZ:
                return "Date";
            case CLOB:
                return "Clob";
            case NCLOB:
                return "NClob";
            case BLOB:
                return "byte[]";
            default:
                return "String";
        }
    }

    public Object read(Connection conn, Object orig) throws SQLException {
        switch (this.dataType) {
            case CLOB:
            case NCLOB:
                return orig.toString();
            default:
                return orig;
        }
    }

    public boolean sameAs(ColumnType targetColumn, ComparePlan plan, CompareResult cr) {
        if (targetColumn == null) {
            cr.getDiff().add(new ColumnDiff(this, targetColumn, ColumnDiff.ActionType.ADD, null));
            cr.setPassed(false);
            cr.addMessage(this.columnName + " not found");
            return false;
        }

        if (!this.columnName.equalsIgnoreCase(targetColumn.getColumnName())) {
            cr.setPassed(false);
            cr.addMessage(this.columnName + " columnName not the same");
            return false;
        }

        this.cs.forEach(c -> c.check(targetColumn, plan, cr));
        return cr.isPassed();
    }

    @Override
    public ColumnType clone() {
        ColumnType ct = new ColumnType();
        ct.setPk(this.isPk());
        ct.setColumnName(getColumnName());
        ct.setColumnSize(getColumnSize());
        ct.setDataType(getDataType());
        ct.setDataTypeCode(getDataTypeCode());
        ct.setDataTypeName(getDataTypeName());
        ct.setDecimalDigits(getDecimalDigits());
        ct.setDefaultValue(getDefaultValue());
        ct.setRemark(getRemark());
        ct.setNullable(isNullable());
        return ct;
    }

    @Override
    public String toString() {
        return String.format("%-30s, %s, %s, %-11s, %4s, %s[%s%s] , %s",
                this.columnName,
                this.pk ? "PK" : "  ",
                this.nullable ? "        " : "NOT NULL",
                this.dataType,
                this.dataTypeCode,
                this.dataTypeName,
                this.columnSize,
                this.decimalDigits > 0 ? "," + this.decimalDigits : "",
                this.remark);
    }

    public boolean checkNullable(ColumnType targetColumn, ComparePlan plan, CompareResult ctx) {
        if (!plan.checkNullable || this.nullable == targetColumn.isNullable()) {
            return true;
        }

        ctx.getDiff().add(new ColumnDiff(this, targetColumn, ColumnDiff.ActionType.ALTER, ColumnDiff.AlterType.NULLABLE));
        ctx.setPassed(false);
        ctx.addMessage(String.format("%s nullable not the same: (%s,%s)",
                this.columnName,
                this.nullable,
                targetColumn.isNullable()));
        return false;
    }

    public boolean checkPK(ColumnType targetColumn, ComparePlan plan, CompareResult ctx) {
        if (this.pk == targetColumn.isPk()) {
            return true;
        }

        ctx.setPassed(false);
        ctx.addMessage(String.format("%s pk not the same: (%s,%s)",
                this.columnName,
                this.pk,
                targetColumn.isPk()));
        return false;
    }

    public boolean checkString(ColumnType targetColumn, ComparePlan plan, CompareResult ctx) {
        if (!isStringType()) {
            return true;
        }

        boolean result = targetColumn.isStringType();
        if (plan.strictVarchar) {
            result = checkDataType(targetColumn, plan, ctx);
        }

        return result ? checkSize(targetColumn, plan, ctx) : result;
    }

    public boolean checkNumber(ColumnType targetColumn, ComparePlan plan, CompareResult ctx) {
        if (!isNumericType()) {
            return true;
        }

        if (!plan.strictNumeric) {
            return targetColumn.isNumericType();
        }

        boolean result = checkDataType(targetColumn, plan, ctx);
        return result ? checkSize(targetColumn, plan, ctx) : result;
    }

    public boolean checkTimestamp(ColumnType targetColumn, ComparePlan plan, CompareResult ctx) {
        if (!isDateTimeType()) {
            return true;
        }

        if (plan.strictDateTime) {
            return checkDataType(targetColumn, plan, ctx);
        }
        else {
            return targetColumn.isDateTimeType();
        }
    }

    public boolean checkDataType(ColumnType targetColumn, ComparePlan plan, CompareResult ctx) {
        // data type
        if (this.dataType != targetColumn.getDataType()) {
            ctx.getDiff().add(new ColumnDiff(this, targetColumn, ColumnDiff.ActionType.ALTER, ColumnDiff.AlterType.DATA_TYPE));
            ctx.setPassed(false);
            ctx.addMessage(String.format("%s dataType not the same: (%s,%s)",
                    this.columnName,
                    this.dataType,
                    targetColumn.getDataType()));
            return false;
        }
        else {
            return true;
        }
    }

    public boolean checkDigit(ColumnType targetColumn, ComparePlan plan, CompareResult ctx) {
        if (this.decimalDigits == targetColumn.getDecimalDigits()) {
            return true;
        }

        ctx.getDiff().add(new ColumnDiff(this, targetColumn, ColumnDiff.ActionType.ALTER, ColumnDiff.AlterType.DATA_TYPE));
        ctx.setPassed(false);
        ctx.addMessage(String.format("%s decimalDigits not the same, (%s:%s,%s:%s)",
                this.columnName,
                this.dataTypeName,
                this.decimalDigits,
                targetColumn.getDataTypeName(),
                targetColumn.getDecimalDigits()));
        return false;
    }

    public boolean checkSize(ColumnType targetColumn, ComparePlan plan, CompareResult ctx) {
        if (!plan.checkDataSize || this.columnSize == targetColumn.getColumnSize()) {
            return true;
        }

        // Oracle always NUMBER, no INTEGER
        if (targetColumn.isInteger() || isInteger()) {
            return true;
        }

        ctx.getDiff().add(new ColumnDiff(this, targetColumn, ColumnDiff.ActionType.ALTER, ColumnDiff.AlterType.DATA_TYPE));
        ctx.setPassed(false);
        ctx.addMessage(String.format("%s columnSize not the same: (%s,%s)",
                this.columnName,
                this.columnSize,
                targetColumn.getColumnSize()));
        return false;
    }

    static interface Compare {

        public boolean check(ColumnType targetColumn, ComparePlan plan, CompareResult ctx);
    }

}
