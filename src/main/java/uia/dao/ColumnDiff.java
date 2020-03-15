package uia.dao;

public class ColumnDiff {

    public enum ActionType {
        ADD, ALTER, DROP
    }

    public enum AlterType {
        DATA_TYPE, NULLABLE,
    }

    public final ColumnType source;

    public final ColumnType target;

    public final ActionType actionType;

    public final AlterType alterType;

    public ColumnDiff(ColumnType source, ColumnType target, ActionType actionType) {
        this(source, target, actionType, null);
    }

    public ColumnDiff(ColumnType source, ColumnType target, ActionType actionType, AlterType alterType) {
        this.source = source;
        this.target = target;;
        this.actionType = actionType;
        this.alterType = alterType;
    }
}
