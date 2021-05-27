package uia.dao.env;

import java.sql.Connection;
import java.sql.SQLException;

public interface Env {

    public String test();

    public Connection create() throws SQLException;

    public default void close() {
    }
}
