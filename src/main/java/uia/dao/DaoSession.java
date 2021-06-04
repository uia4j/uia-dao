package uia.dao;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

public class DaoSession implements Closeable {

    public final Connection conn;

    private final DaoFactory factory;

    DaoSession(DaoFactory factory, Connection conn) {
        this.factory = factory;
        this.conn = conn;
    }

    public Date toUTC(Date local) {
        return this.factory.toUTC(local);
    }

    public Date fromUTC(Date utc) {
        return this.factory.fromUTC(utc);
    }

    public void setAutoCommit(boolean autoCommit) throws SQLException {
        this.conn.setAutoCommit(autoCommit);
    }

    public void commit() throws SQLException {
        this.conn.commit();
    }

    public void rollback() throws SQLException {
        this.conn.rollback();
    }

    public <T> TableDao<T> forTable(Class<T> dtoClz) {
        return new TableDao<T>(this.conn, this.factory.forTable(dtoClz));
    }

    public <T> ViewDao<T> forView(Class<T> dtoClz) {
        return new ViewDao<T>(this.conn, this.factory.forView(dtoClz));
    }

    public <T extends TableDao<?>> T tableDao(Class<T> daoClz) throws DaoException {
        return this.factory.proxyTableDao(daoClz, this.conn);
    }

    public <T extends ViewDao<?>> T viewDao(Class<T> daoClz) throws DaoException {
        return this.factory.proxyViewDao(daoClz, this.conn);
    }

    @Override
    public void close() throws IOException {
        try {
            this.conn.close();
        }
        catch (SQLException e) {
            throw new IOException(e);
        }
    }
}
