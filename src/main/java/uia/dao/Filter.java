package uia.dao;

import java.sql.ResultSet;
import java.util.Collection;

public interface Filter {

    public static final Filter ALL = rs -> true;

    public boolean accept(ResultSet rs);

    public static class Text implements Filter {

        private final String column;

        private final String value;

        public Text(String column, String value) {
            this.column = column;
            this.value = value;
        }

        @Override
        public boolean accept(ResultSet rs) {
            try {
                return this.value.equals(rs.getString(this.column));
            }
            catch (Exception ex) {
                return true;
            }
        }

    }

    public static class TextSet implements Filter {

        private final String column;

        private final Collection<String> values;

        private final boolean include;

        public TextSet(String column, Collection<String> values) {
            this(column, values, true);
        }

        public TextSet(String column, Collection<String> values, boolean include) {
            this.column = column;
            this.values = values;
            this.include = include;
        }

        @Override
        public boolean accept(ResultSet rs) {
            try {
                return this.include
                        ? this.values.contains(rs.getString(this.column))
                        : !this.values.contains(rs.getString(this.column));
            }
            catch (Exception ex) {
                return true;
            }
        }

    }
}
