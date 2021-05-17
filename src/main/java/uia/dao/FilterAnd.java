package uia.dao;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class FilterAnd implements Filter {

    private List<Filter> filters;

    public static FilterAnd create(Filter filter) {
        return new FilterAnd().add(filter);
    }

    public FilterAnd() {
        this(new ArrayList<>());
    }

    public FilterAnd(List<Filter> filters) {
        this.filters = filters;
    }

    public FilterAnd add(Filter filter) {
        this.filters.add(filter);
        return this;
    }

    @Override
    public boolean accept(ResultSet rs) {
        return !this.filters.stream()
                .filter(f -> !f.accept(rs))
                .findAny()
                .isPresent();
    }
}
