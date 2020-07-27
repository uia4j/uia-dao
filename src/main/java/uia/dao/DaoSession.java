package uia.dao;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class DaoSession implements Closeable {

    private final DaoFactory factory;

    private final Connection conn;

    DaoSession(DaoFactory factory, Connection conn) {
        this.factory = factory;
        this.conn = conn;
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

    public <T extends TableDao<?>> T forTable(Class<T> daoClz) throws Exception {
        return this.factory.proxyTableDao(daoClz, this.conn);
    }

    public <T extends ViewDao<?>> T forView(Class<T> daoClz) throws Exception {
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
