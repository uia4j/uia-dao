package uia.dao;

import java.sql.ResultSet;

public interface ObjectMapper {

    public Object read(ResultSet rs);
}
