package uia.dao;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import uia.dao.annotation.DeleteInfo;
import uia.dao.annotation.SelectInfo;
import uia.dao.annotation.UpdateInfo;

import javassist.util.proxy.ProxyFactory;

public final class ProxyDao {

    private Connection conn;

    ProxyDao() {
    }

    @SuppressWarnings("unchecked")
    <T> T bind(Class<T> absclz, Connection conn, TableDaoHelper<?> helper) throws DaoException {
        this.conn = conn;
        ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(absclz);
        factory.setFilter(m -> Modifier.isAbstract(m.getModifiers()));

        try {
            T result = (T) factory.create(
                    new Class<?>[] { Connection.class, TableDaoHelper.class },
                    new Object[] { conn, helper },
                    this::runTable);
            return result;
        }
        catch (Exception ex) {
            throw new DaoException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    <T> T bind(Class<T> absclz, Connection conn, ViewDaoHelper<?> helper) throws DaoException {
        this.conn = conn;
        ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(absclz);
        factory.setFilter(m -> Modifier.isAbstract(m.getModifiers()));

        try {
            return (T) factory.create(
                    new Class<?>[] { Connection.class, ViewDaoHelper.class },
                    new Object[] { conn, helper },
                    this::runView);
        }
        catch (Exception ex) {
            throw new DaoException(ex);
        }
    }

    @SuppressWarnings("rawtypes")
    private Object runTable(Object self, Method proxyMethod, Method proceed, Object[] args) throws Throwable {
        TableDao dao = (TableDao) self;

        // select
        SelectInfo selectInfo = proxyMethod.getDeclaredAnnotation(SelectInfo.class);
        if (selectInfo != null) {
            boolean list = List.class.isAssignableFrom(proxyMethod.getReturnType());
            DaoMethod<?> method = selectInfo.join()
                    ? dao.tableHelper.forSelectX()
                    : dao.tableHelper.forSelect();
            final String sql = method.getSql() + selectInfo.sql();
            try (PreparedStatement ps = this.conn.prepareStatement(sql)) {
                for (int i = 0; i < args.length; i++) {
                    dao.tableHelper.getFactory()
                            .getColumnWriter(args[i] == null ? "object" : args[i].getClass().getSimpleName())
                            .write(ps, i + 1, args[i]);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    return list ? method.toList(rs, selectInfo.top()) : method.toOne(rs);
                }
            }
        }

        // update
        UpdateInfo updateInfo = proxyMethod.getDeclaredAnnotation(UpdateInfo.class);
        if (updateInfo != null) {
            try (PreparedStatement ps = this.conn.prepareStatement(String.format("UPDATE %s %s",
                    dao.tableHelper.getTableName(),
                    updateInfo.sql()))) {
                for (int i = 0; i < args.length; i++) {
                    dao.tableHelper.getFactory()
                            .getColumnWriter(args[i] == null ? "object" : args[i].getClass().getSimpleName())
                            .write(ps, i + 1, args[i]);
                }
                return ps.executeUpdate();
            }
        }

        // delete
        DeleteInfo deleteInfo = proxyMethod.getDeclaredAnnotation(DeleteInfo.class);
        if (deleteInfo != null) {
            DaoMethod<?> method = dao.tableHelper.forDelete();
            try (PreparedStatement ps = this.conn.prepareStatement(method.getSql() + deleteInfo.sql())) {
                for (int i = 0; i < args.length; i++) {
                    dao.tableHelper.getFactory()
                            .getColumnWriter(args[i] == null ? "object" : args[i].getClass().getSimpleName())
                            .write(ps, i + 1, args[i]);
                }
                return ps.executeUpdate();
            }
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    private Object runView(Object self, Method proxyMethod, Method proceed, Object[] args) throws Throwable {
        boolean list = List.class.isAssignableFrom(proxyMethod.getReturnType());
        SelectInfo selectInfo = proxyMethod.getDeclaredAnnotation(SelectInfo.class);
        if (selectInfo == null) {
            return list ? new ArrayList<>() : null;
        }

        ViewDao dao = (ViewDao) self;
        DaoMethod<?> method = selectInfo.join()
                ? dao.viewHelper.forSelectX()
                : dao.viewHelper.forSelect();
        try (PreparedStatement ps = this.conn.prepareStatement(method.getSql() + selectInfo.sql())) {
            for (int i = 0; i < args.length; i++) {
                dao.viewHelper.getFactory()
                        .getColumnWriter(args[i] == null ? "object" : args[i].getClass().getSimpleName())
                        .write(ps, i + 1, args[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return list ? method.toList(rs) : method.toOne(rs);
            }
        }
    }
}
