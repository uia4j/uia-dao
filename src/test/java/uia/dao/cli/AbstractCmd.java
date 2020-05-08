package uia.dao.cli;

import java.sql.SQLException;

import uia.dao.Database;

public abstract class AbstractCmd {

    protected Database byType(String name) throws SQLException {
        return DatabaseSource.byType(name);
    }

    protected Database byReal(String name) throws SQLException {
        return DatabaseSource.byReal(name);
    }
}
