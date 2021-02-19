package uia.dao;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;
import org.postgresql.util.PGobject;

import com.google.gson.GsonBuilder;

import uia.dao.ColumnType.DataType;
import uia.dao.sample2.Sample2;

public class DaoEnvTest {
	
	@Test 
	public void testPool() throws Exception {
		DaoEnv env = DaoEnv
				.pool(false, "uia.dao.sample1")
				.config("jdbc:postgresql://localhost:5432/postgres", "postgres", "pgAdmin", "public");
		try(DaoSession session = env.createSession()) {
		}

	}
	
	@Test
	public void testDaoEnvImpl() throws Exception {
		DaoEnv nnv = new DaoEnvImpl()
				.config("jdbc:postgresql://localhost:5432/postgres",  "postgres", "pgAdmin", null);

		try(DaoSession session = nnv.createSession()) {
			Sample2 row = new Sample2();
			row.setC1(UUID.randomUUID().toString());
			row.setC3(new BigDecimal("12.4863"));
			row.setC4(new Date());
			row.setC5(new Date());
			row.getC6().put("id", "12345678");
			row.getC6().put("name", "kyle");
			row.setC7("{ \"a\": \"\123\", \"b\": 456}");
			TableDao<Sample2> dao = session.forTable(Sample2.class);
			dao.insert(row);
			dao.selectAll().forEach(s -> {
				System.out.println(s.getC6() + ", " + s.getC7());
			});
		}
	}
	public static class DaoEnvImpl extends DaoEnv implements DaoColumnReader, DaoColumnWriter {
		
		public DaoEnvImpl() throws Exception {
			super(DaoEnv.POSTGRE, false);
		}

		@Override
		protected void initialFactory(DaoFactory factory) throws Exception {
			factory.register("raws", DataType.JSON, this, this);	// before load package.
			factory.load("uia.dao.sample2");
		}

		@Override
		public void write(PreparedStatement ps, int index, Object value) throws SQLException {
			PGobject jsonObject = new PGobject();
			jsonObject.setType("json");
			jsonObject.setValue(new GsonBuilder().create().toJson(value));
			ps.setObject(index,  jsonObject);
		}

		@Override
		public Object read(ResultSet rs, int index) throws SQLException {
			String json = rs.getString(index);
			return new GsonBuilder().create().fromJson(json, Map.class);
		}
	}
}

