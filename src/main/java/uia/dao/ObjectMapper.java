package uia.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public interface ObjectMapper<T> {

    public T read(ResultSet rs) throws SQLException;

    public static class Null implements ObjectMapper<Object> {

        @Override
        public Object read(ResultSet rs) throws SQLException {
            return null;
        }
    }

    public static class StrList implements ObjectMapper<List<String>> {

        @Override
        public List<String> read(ResultSet rs) throws SQLException {
            ArrayList<String> values = new ArrayList<>();
            while (rs.next()) {
                values.add(rs.getString(1));
            }
            return values;
        }

    }
}
