package uia.dao;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import uia.dao.where.Where;

public class DaoSession implements Closeable {

    public final Connection conn;

    private final DaoFactory factory;

    protected DaoSession(DaoFactory factory, Connection conn) {
        this.factory = factory;
        this.conn = conn;
    }

    public <T> List<T> all(Class<T> dtoTable) throws SQLException, DaoException {
        return forTable(dtoTable).selectAll();
    }

    public <K, T> Map<K, List<T>> all(Class<T> dtoTable, Function<T, K> classifier) throws SQLException, DaoException {
        return forTable(dtoTable).selectAll()
                .stream()
                .collect(Collectors.groupingBy(classifier));
    }

    public <T> List<T> where(Class<T> dtoTable, Where where) throws SQLException, DaoException {
        return forTable(dtoTable).select(where);
    }

    public <T> List<T> where(Class<T> dtoTable, Where where, String orders) throws SQLException, DaoException {
        return forTable(dtoTable).select(where, orders);
    }

    public <T> int insert(Class<T> dtoTable, T data) throws SQLException, DaoException {
        return forTable(dtoTable).insert(data);
    }

    public <T> int update(Class<T> dtoTable, T data) throws SQLException, DaoException {
        return forTable(dtoTable).update(data);
    }

    public <T> T one(Class<T> dtoTable, Object... pks) throws SQLException, DaoException {
        return forTable(dtoTable).selectByPK(pks);
    }

    public <T> int delete(Class<T> dtoTable, Object... pks) throws SQLException, DaoException {
        return forTable(dtoTable).deleteByPK(pks);
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
