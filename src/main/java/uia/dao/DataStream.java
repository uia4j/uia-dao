package uia.dao;

import java.io.Closeable;
import java.io.IOException;
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
 * @param <T>
 */
public interface DataStream<T> extends Closeable {

    public static <T> DataStream<T> empty() {
        return new Empty<>();
    }

    public T next() throws SQLException, DaoException;

    public static class Empty<T> implements DataStream<T> {

        @Override
        public T next() throws SQLException, DaoException {
            return null;
        }

        @Override
        public void close() throws IOException {
        }
    }
}
