package uia.dao.pms;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import uia.dao.DaoException;
import uia.dao.TableDao;
import uia.dao.TableDaoHelper;
import uia.dao.annotation.DaoInfo;
import uia.dao.annotation.SelectInfo;

@DaoInfo(type = Lookup.class)
public abstract class LookupDao extends TableDao<Lookup> {

    public LookupDao(Connection conn, TableDaoHelper<Lookup> helper) {
        super(conn, helper);
    }

    @SelectInfo(sql = "WHERE id=? ORDER BY sub_id,param_name")
    public abstract List<Lookup> select(String id) throws SQLException, DaoException;

    @SelectInfo(sql = "WHERE id=? AND sub_id=? ORDER BY param_name")
    public abstract List<Lookup> select(String id, String subId) throws SQLException, DaoException;
}
