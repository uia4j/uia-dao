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

import java.sql.Clob;
import java.sql.Connection;
import java.sql.NClob;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * The structure of a column.
 *
 * @author Kyle K. Lin
 *
 */
public abstract class ColumnType {

    public enum DataType {

        /**
         * varchar
         */
        VARCHAR,

        /**
         * nvarchar
         */
        NVARCHAR,

        /**
         * varchar2
         */
        VARCHAR2,

        /**
         * nvarchar2
         */
        NVARCHAR2,

        /**
         * integer
         */
        INTEGER,

        /**
         * long
         */
        LONG,

        /**
         * numeroc
         */
        NUMERIC,

        /**
         * float
         */
        FLOAT,

        DOUBLE,

        /**
         * timestamp
         */
        TIMESTAMP,

        /**
         * date
         */
        DATE,

        /**
         * time
         */
        TIME,

        /**
         * blob
         */
        BLOB,

        /**
         * clob
         */
        CLOB,

        /**
         * nclob
         */
        NCLOB,

        /**
         * others
         */
        OTHERS;

        DataType() {
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
     * Returns the Java type for this column.
     *
     * @return Type name of Java.
     */
    public String getJavaTypeName() {
        switch (this.dataType) {
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
                Clob clob = conn.createClob();
                clob.setString(1, orig.toString());
                return orig;
            case NCLOB:
                NClob nclob = conn.createNClob();
                nclob.setString(1, orig.toString());
                return orig;
            default:
                return orig;
        }
    }

    public boolean sameAs(ColumnType targetColumn, ComparePlan plan, CompareResult cr) {
        if (targetColumn == null) {
            cr.getDiff().add(new ColumnDiff(this, ColumnDiff.ActionType.ADD, null));
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

    String genPsSet(int index) {
        String propertyName = CamelNaming.upper(this.columnName);
        switch (this.dataType) {
            case DATE:
            case TIME:
            case TIMESTAMP:
                return String.format("ps.setTimestamp(%s, new Timestamp(data.get%s().getTime()));",
                        index,
                        propertyName);
            case INTEGER:
                return String.format("ps.setInt(%s, data.get%s());",
                        index,
                        propertyName);
            case LONG:
                return String.format("ps.setLong(%s, data.get%s());",
                        index,
                        propertyName);
            case NUMERIC:
            case FLOAT:
            case DOUBLE:
                return String.format("ps.setBigDecimal(%s, data.get%s());",
                        index,
                        propertyName);
            case CLOB:
                return String.format("ps.setClob(%s, data.get%s());",
                        index,
                        propertyName);
            case NCLOB:
                return String.format("ps.setNClob(%s, data.get%s());",
                        index,
                        propertyName);
            case BLOB:
                return String.format("ps.setBlob(%s, data.get%s());",
                        index,
                        propertyName);

            default:
                return String.format("ps.setString(%s, data.get%s());",
                        index,
                        propertyName);
        }
    }

    String genPsSetEx(int index) {
        String propertyName = CamelNaming.lower(this.columnName);
        switch (this.dataType) {
            case DATE:
            case TIME:
            case TIMESTAMP:
                return String.format("ps.setTimestamp(%s, new Timestamp(%s.getTime()));",
                        index,
                        propertyName);
            case INTEGER:
                return String.format("ps.setInt(%s, %s);",
                        index,
                        propertyName);
            case LONG:
                return String.format("ps.setLong(%s, %s);",
                        index,
                        propertyName);
            case NUMERIC:
            case FLOAT:
            case DOUBLE:
                return String.format("ps.setBigDecimal(%s, %s);",
                        index,
                        propertyName);
            case CLOB:
                return String.format("ps.setClob(%s, %s);",
                        index,
                        propertyName);
            case NCLOB:
                return String.format("ps.setNClob(%s, %s);",
                        index,
                        propertyName);
            case BLOB:
                return String.format("ps.setBlob(%s, %s);",
                        index,
                        propertyName);
            default:
                return String.format("ps.setString(%s, %s);",
                        index,
                        propertyName);
        }
    }

    String genRsGet(String index) {
        String type = null;
        switch (this.dataType) {
            case INTEGER:
                type = "Int";
                break;
            case LONG:
                type = "Long";
                break;
            case NUMERIC:
            case FLOAT:
            case DOUBLE:
                type = "BigDecimal";
                break;
            case DATE:
            case TIME:
            case TIMESTAMP:
                type = "Timestamp";
                break;
            case CLOB:
                type = "Clob";
                break;
            case NCLOB:
                type = "NClob";
                break;
            case BLOB:
                type = "Blob";
                break;
            default:
                type = "String";
        }

        return String.format("data.set%s(rs.get%s(%s));",
                CamelNaming.upper(this.columnName),
                type,
                index);
    }

    @Override
    public String toString() {
        return String.format("%-30s, pk:%-5s, %-9s, %4s:%-13s [%s], %s",
                this.columnName,
                this.pk,
                this.dataType,
                this.dataTypeCode,
                this.dataTypeName,
                this.columnSize,
                this.remark);
    }

    public boolean checkNullable(ColumnType targetColumn, ComparePlan plan, CompareResult ctx) {
        if (!plan.checkNullable || this.nullable == targetColumn.isNullable()) {
            return true;
        }

        ctx.getDiff().add(new ColumnDiff(this, ColumnDiff.ActionType.ALTER, ColumnDiff.AlterType.NULLABLE));
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
            result = chdckDataType(targetColumn, plan, ctx);
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

        boolean result = chdckDataType(targetColumn, plan, ctx);
        return result ? checkSize(targetColumn, plan, ctx) : result;
    }

    public boolean checkTimestamp(ColumnType targetColumn, ComparePlan plan, CompareResult ctx) {
        if (!isDateTimeType()) {
            return true;
        }

        if (plan.strictDateTime) {
            return chdckDataType(targetColumn, plan, ctx);
        }
        else {
            return targetColumn.isDateTimeType();
        }
    }

    public boolean chdckDataType(ColumnType targetColumn, ComparePlan plan, CompareResult ctx) {
        // data type
        if (this.dataType != targetColumn.getDataType()) {
            ctx.getDiff().add(new ColumnDiff(this, ColumnDiff.ActionType.ALTER, ColumnDiff.AlterType.DATA_TYPE));
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
