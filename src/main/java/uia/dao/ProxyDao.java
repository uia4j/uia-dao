package uia.dao;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javassist.util.proxy.ProxyFactory;
import uia.dao.annotation.SelectInfo;

public final class ProxyDao  {
	
	private Connection conn;
	
	ProxyDao() {
	}
	
	@SuppressWarnings("unchecked")
	<T> T bind(Class<T> absclz, Connection conn, TableDaoHelper<?> helper) throws Exception {
		this.conn = conn;
		ProxyFactory factory = new ProxyFactory();
		factory.setSuperclass(absclz);
		factory.setFilter(m -> Modifier.isAbstract(m.getModifiers()));
		
		return (T) factory.create(
				new Class<?>[] {Connection .class, TableDaoHelper.class}, 
				new Object[] { conn, helper },
				this::invokeTable);
	}
	
	@SuppressWarnings("unchecked")
	<T> T bind(Class<T> absclz, Connection conn, ViewDaoHelper<?> helper) throws Exception {
		this.conn = conn;
		ProxyFactory factory = new ProxyFactory();
		factory.setSuperclass(absclz);
		factory.setFilter(m -> Modifier.isAbstract(m.getModifiers()));
		
		return (T) factory.create(
				new Class<?>[] {Connection .class, ViewDaoHelper.class}, 
				new Object[] { conn, helper },
				this::invokeView);
	}

	@SuppressWarnings("rawtypes")
	private Object invokeTable(Object self, Method proxyMethod, Method proceed, Object[] args) throws Throwable {
		boolean list = List.class.isAssignableFrom(proxyMethod.getReturnType());
		SelectInfo selectInfo = proxyMethod.getDeclaredAnnotation(SelectInfo.class);
		if(selectInfo == null) {
			return list ? new ArrayList<>() : null;
		}
		
		TableDao dao = (TableDao)self;
		DaoMethod<?> method = dao.tableHelper.forSelect();
        try (PreparedStatement ps = this.conn.prepareStatement(method.getSql() + selectInfo.sql())) {
        	for(int i=0; i<args.length; i++) {
	            ps.setObject(i + 1, args[i]);
        	}
            try (ResultSet rs = ps.executeQuery()) {
                return list ? method.toList(rs) :  method.toOne(rs);
            }
        }
	}

	@SuppressWarnings("rawtypes")
	private Object invokeView(Object self, Method proxyMethod, Method proceed, Object[] args) throws Throwable {
		boolean list = List.class.isAssignableFrom(proxyMethod.getReturnType());
		SelectInfo selectInfo = proxyMethod.getDeclaredAnnotation(SelectInfo.class);
		if(selectInfo == null) {
			return list ? new ArrayList<>() : null;
		}
		
		ViewDao dao = (ViewDao)self;
		DaoMethod<?> method = dao.viewHelper.forSelect();
        try (PreparedStatement ps = this.conn.prepareStatement(method.getSql() + selectInfo.sql())) {
        	for(int i=0; i<args.length; i++) {
	            ps.setObject(i + 1, args[i]);
        	}
            try (ResultSet rs = ps.executeQuery()) {
                return list ? method.toList(rs) :  method.toOne(rs);
            }
        }
	}
}
