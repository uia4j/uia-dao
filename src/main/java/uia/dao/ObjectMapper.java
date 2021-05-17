package uia.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ObjectMapper<T> {

    public T read(ResultSet rs) throws SQLException;

    public static class Null implements ObjectMapper<Object> {

        @Override
        public Object read(ResultSet rs) throws SQLException {
            return null;
        }

    }
}
