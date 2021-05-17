package uia.dao;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class FilterOr implements Filter {

    private List<Filter> filters;

    public static FilterOr create(Filter filter) {
        return new FilterOr().add(filter);
    }

    public FilterOr() {
        this(new ArrayList<>());
    }

    public FilterOr(List<Filter> filters) {
        this.filters = filters;
    }

    public FilterOr add(Filter filter) {
        this.filters.add(filter);
        return this;
    }

    @Override
    public boolean accept(ResultSet rs) {
        return this.filters.stream()
                .filter(f -> f.accept(rs))
                .findAny()
                .isPresent();
    }

}
