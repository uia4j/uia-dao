package uia.dao;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * <p>
 * The stream implementation has instances of PreparedStatement and ResultSet.
 * After iterating all records, you need to close the stream manually.
 * </p>
 * <pre>
 * {@code
 *
 * ResultSetStream<T> stream = tableDao.select(...);
 *
 * T value = null;
 * while((value = stream.next()) != null) {
 *     // ...
 * }
 *
 * stream.close();
 *
 * }
 * </pre>
 *
 * @author Kan
 *
 * @param <T> The data type.
 */
public class ResultSetStream<T> implements DataStream<T> {

    private final DaoMethod<T> method;

    private final PreparedStatement ps;

    private final ResultSet rs;

    public ResultSetStream(DaoMethod<T> method, PreparedStatement statement, ResultSet rs) {
        this.ps = statement;
        this.method = method;
        this.rs = rs;
    }

    @Override
    public T next() throws SQLException, DaoException {
        T t = this.method.toOne(this.rs);
        return t;
    }

    @Override
    public void close() throws IOException {
        try {
            this.rs.close();
        }
        catch (SQLException e) {
        }
        try {
            this.ps.close();
        }
        catch (SQLException e) {
        }
    }
}
