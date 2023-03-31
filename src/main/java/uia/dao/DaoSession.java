package uia.dao;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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

    public <T> T one(Class<T> dtoTable, Object... pks) throws SQLException, DaoException {
        return forTable(dtoTable).selectByPK(pks);
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

    public <T> int[] insert(Class<T> dtoTable, List<T> data) throws SQLException, DaoException {
        return forTable(dtoTable).insert(data);
    }

    public <T> int update(Class<T> dtoTable, T data) throws SQLException, DaoException {
        return forTable(dtoTable).update(data);
    }

    public <T> int[] update(Class<T> dtoTable, List<T> data) throws SQLException, DaoException {
        return forTable(dtoTable).update(data);
    }

    public <T> int delete(Class<T> dtoTable, Object... pks) throws SQLException, DaoException {
        return forTable(dtoTable).deleteByPK(pks);
    }

    public <T> int deleteAll(Class<T> dtoTable) throws SQLException, DaoException {
        return forTable(dtoTable).deleteAll();
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

    public List<Object[]> query(String sql) throws SQLException {
        ArrayList<Object[]> result = new ArrayList<>();
        Statement stat = this.conn.createStatement();
        try (ResultSet rs = stat.executeQuery(sql)) {
            int c = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                Object[] values = new Object[c];
                for (int i = 0; i < c; i++) {
                    Object obj = rs.getObject(i + 1);
                    if (obj instanceof Date) {
                        obj = fromUTC((Date) obj);
                    }
                    values[i] = obj;
                }
                result.add(values);
            }
        }
        return result;
    }

    @Override
    public void close() throws IOException {
        try {
            if (!this.conn.isClosed()) {
                this.conn.close();
            }
        }
        catch (SQLException e) {
            throw new IOException(e);
        }
    }
}
