package uia.dao;

public class ColumnDiff {

    public enum ActionType {
        ADD, ALTER, DROP
    }

    public enum AlterType {
        DATA_TYPE, NULLABLE,
    }

    public final ColumnType column;

    public final ActionType actionType;

    public final AlterType alterType;

    public ColumnDiff(ColumnType column, ActionType actionType) {
        this(column, actionType, null);
    }

    public ColumnDiff(ColumnType column, ActionType actionType, AlterType alterType) {
        this.column = column;
        this.actionType = actionType;
        this.alterType = alterType;
    }
}
