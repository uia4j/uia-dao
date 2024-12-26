package uia.dao;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import uia.dao.annotation.DeleteInfo;
import uia.dao.annotation.SelectCountInfo;
import uia.dao.annotation.SelectInfo;
import uia.dao.annotation.UpdateInfo;

import javassist.util.proxy.ProxyFactory;

public final class ProxyDao {

    private Connection conn;

    ProxyDao() {
    }

    @SuppressWarnings("unchecked")
    <T> T bind(Class<T> absClz, Connection conn, TableDaoHelper<?> helper) throws DaoException {
        this.conn = conn;
        ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(absClz);
        factory.setFilter(m -> Modifier.isAbstract(m.getModifiers()));

        try {
            T result = (T) factory.create(
                    new Class<?>[] {},
                    new Object[] {},
                    this::runTable);

            Class<?> clz = result.getClass();
            while (clz != null && clz != TableDao.class) {
                clz = clz.getSuperclass();
            }
            if (clz == null) {
                return null;
            }
            Field f1 = clz.getDeclaredField("conn");
            f1.setAccessible(true);
            f1.set(result, conn);
            Field f2 = clz.getDeclaredField("tableHelper");
            f2.setAccessible(true);
            f2.set(result, helper);

            return result;
        }
        catch (Exception ex) {
            throw new DaoException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    <T> T bind(Class<T> absClz, Connection conn, ViewDaoHelper<?> helper) throws DaoException {
        this.conn = conn;
        ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(absClz);
        factory.setFilter(m -> Modifier.isAbstract(m.getModifiers()));

        try {
            T result = (T) factory.create(
                    new Class<?>[] {},
                    new Object[] {},
                    this::runView);

            Class<?> clz = result.getClass();
            while (clz != null && clz != ViewDao.class) {
                clz = clz.getSuperclass();
            }
            if (clz == null) {
                return null;
            }
            Field f1 = clz.getDeclaredField("conn");
            f1.setAccessible(true);
            f1.set(result, conn);
            Field f2 = clz.getDeclaredField("viewHelper");
            f2.setAccessible(true);
            f2.set(result, helper);
            return result;
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
            Class<?> mapperClz = selectInfo.mapper();
            DaoMethod<?> method = selectInfo.join()
                    ? dao.tableHelper.forSelectX()
                    : dao.tableHelper.forSelect();
            if (!ObjectMapper.class.isAssignableFrom(mapperClz)) {
                method = selectInfo.join()
                        ? dao.tableHelper.getFactory().forTable(mapperClz).forSelectX()
                        : dao.tableHelper.getFactory().forTable(mapperClz).forSelect();
            }

            final String sql = method.getSql() + " " + selectInfo.sql();

            // stream
            boolean stream = DataStream.class.isAssignableFrom(proxyMethod.getReturnType());
            if (stream) {
                PreparedStatement ps = this.conn.prepareStatement(sql);
                int r = 1;
                for (int i = 0; i < args.length; i++) {
                    Object v = args[i];
                    dao.tableHelper.getFactory()
                            .getColumnWriter(v == null ? "object" : v.getClass().getSimpleName())
                            .write(ps, r++, v);
                }
                ResultSet rs = ps.executeQuery();
                return new ResultSetStream<>(method, ps, rs);
            }

            // normal
            try (PreparedStatement ps = this.conn.prepareStatement(sql)) {
                int r = 1;
                Filter filter = Filter.ALL;
                for (int i = 0; i < args.length; i++) {
                    Object v = args[i];
                    if (v != null && v instanceof Filter) {
                        filter = (Filter) v;
                        continue;
                    }
                    else {
                        dao.tableHelper.getFactory()
                                .getColumnWriter(v == null ? "object" : v.getClass().getSimpleName())
                                .write(ps, r++, v);
                    }
                }
                if (ObjectMapper.class.isAssignableFrom(mapperClz) && mapperClz != ObjectMapper.Null.class) {
                    ObjectMapper mapper = (ObjectMapper) mapperClz.newInstance();
                    try (ResultSet rs = ps.executeQuery()) {
                        return mapper.read(rs);
                    }
                }
                else {
                    boolean list = List.class.isAssignableFrom(proxyMethod.getReturnType());
                    try (ResultSet rs = ps.executeQuery()) {
                        return list ? method.toList(rs, filter, selectInfo.top()) : method.toOne(rs);
                    }
                }
            }
        }

        SelectCountInfo countInfo = proxyMethod.getDeclaredAnnotation(SelectCountInfo.class);
        if (countInfo != null) {
            final String sql = String.format("SELECT count(*) n FROM %s %s", dao.tableHelper.getTableName(), countInfo.sql());
            try (PreparedStatement ps = this.conn.prepareStatement(sql)) {
                int r = 1;
                for (int i = 0; i < args.length; i++) {
                    Object v = args[i];
                    dao.tableHelper.getFactory()
                            .getColumnWriter(v == null ? "object" : v.getClass().getSimpleName())
                            .write(ps, r++, v);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? rs.getInt(0) : 0;
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
            try (PreparedStatement ps = this.conn.prepareStatement(method.getSql() + " " + deleteInfo.sql())) {
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
        boolean stream = DataStream.class.isAssignableFrom(proxyMethod.getReturnType());
        boolean list = List.class.isAssignableFrom(proxyMethod.getReturnType());

        SelectInfo selectInfo = proxyMethod.getDeclaredAnnotation(SelectInfo.class);
        if (selectInfo == null) {
            return stream ? DataStream.empty() : list ? new ArrayList<>() : null;
        }

        Class<?> mapperClz = selectInfo.mapper();
        ViewDao dao = (ViewDao) self;
        DaoMethod<?> method = selectInfo.join()
                ? dao.viewHelper.forSelectX()
                : dao.viewHelper.forSelect();
        if (!ObjectMapper.class.isAssignableFrom(mapperClz)) {
            method = selectInfo.join()
                    ? dao.viewHelper.getFactory().forView(mapperClz).forSelectX()
                    : dao.viewHelper.getFactory().forView(mapperClz).forSelect();
        }

        final String sql = method.getSql() + " " + selectInfo.sql();

        // stream
        if (stream) {
            PreparedStatement ps = this.conn.prepareStatement(sql);
            int r = 1;
            for (int i = 0; i < args.length; i++) {
                Object v = args[i];
                dao.viewHelper.getFactory()
                        .getColumnWriter(v == null ? "object" : v.getClass().getSimpleName())
                        .write(ps, r++, v);
            }
            ResultSet rs = ps.executeQuery();
            return new ResultSetStream<>(method, ps, rs);
        }

        // normal
        try (PreparedStatement ps = this.conn.prepareStatement(sql)) {
            int r = 1;
            Filter filter = Filter.ALL;
            for (int i = 0; i < args.length; i++) {
                Object v = args[i];
                if (v != null && v instanceof Filter) {
                    filter = (Filter) v;
                }
                else {
                    dao.viewHelper.getFactory()
                            .getColumnWriter(v == null ? "object" : v.getClass().getSimpleName())
                            .write(ps, r++, v);
                }
            }
            if (ObjectMapper.class.isAssignableFrom(mapperClz) && mapperClz != ObjectMapper.Null.class) {
                ObjectMapper mapper = (ObjectMapper) mapperClz.newInstance();
                try (ResultSet rs = ps.executeQuery()) {
                    return mapper.read(rs);
                }
            }
            else {
                try (ResultSet rs = ps.executeQuery()) {
                    return list ? method.toList(rs, filter, selectInfo.top()) : method.toOne(rs);
                }
            }
        }
    }
}
