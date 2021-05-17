package uia.dao;

import java.sql.ResultSet;

public interface Mapper<T> {

    public T convert(ResultSet rs);
}
