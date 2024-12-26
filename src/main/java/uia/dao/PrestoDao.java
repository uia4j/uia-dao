package uia.dao;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import uia.dao.annotation.LakeColumnInfo;
import uia.dao.annotation.LakeSelectInfo;
import uia.dao.where.Where;

public class PrestoDao implements Closeable {

    private final DaoFactory factory;

    private Connection conn;

    public PrestoDao(String trustStorePath, String trustStorePwd) {
        this.factory = new DaoFactory(false, true);

        Properties props = System.getProperties();
        props.setProperty("javax.net.ssl.trustStore", trustStorePath);
        props.setProperty("javax.net.ssl.trustStorePassword", trustStorePwd);
        System.setProperties(props);

    }

    public void connect(String url, String user, String pwd) throws SQLException {
        if (this.conn == null) {
            Properties props = new Properties();
            props.setProperty("user", "kyle");
            props.setProperty("password", "K@nlin5953");
            props.setProperty("SSL", "true");
            this.conn = DriverManager.getConnection(url, props);
        }
    }

    public <T> List<T> select(Class<T> clz, Where where, String orders) throws Exception {
        LakeSelectInfo si = clz.getDeclaredAnnotation(LakeSelectInfo.class);
        if (si == null) {
            throw new NullPointerException(clz.getName() + " @LakeSelect annotation not found");
        }

        Field[] fs = clz.getDeclaredFields();
        PrestoColumn[] cols = new PrestoColumn[fs.length];
        int max = 0;
        for (Field f : fs) {
            LakeColumnInfo ci = f.getDeclaredAnnotation(LakeColumnInfo.class);
            if (ci != null) {
                f.setAccessible(true);
                cols[ci.index()] = new PrestoColumn(f, ci);
                max = Math.max(ci.index(), max);
            }
        }

        ArrayList<T> result = new ArrayList<>();
        SelectStatement sql = new SelectStatement(si.sql())
                .where(where)
                .orderBy(orders);
        try (PreparedStatement ps = sql.prepare(this.conn)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    T one = clz.newInstance();
                    for (int i = 0; i <= max; i++) {
                        PrestoColumn col = cols[i];
                        if (col == null) {
                            throw new NullPointerException(clz.getName() + " @LakeColumn index=" + i + " not found");
                        }
                        DaoColumnReader reader = this.factory.getColumnReader(col.typeName());
                        col.apply(one, reader.read(rs, i + 1));
                    }
                    result.add(one);
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }

    @Override
    public void close() throws IOException {
        if (this.conn != null) {
            try {
                this.conn.close();
            }
            catch (SQLException e) {
                throw new IOException(e);
            }
        }
        this.conn = null;
    }
}
