package uia.dao.pms;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import uia.dao.DaoException;
import uia.dao.TableDao;
import uia.dao.TableDaoHelper;
import uia.dao.annotation.DaoInfo;
import uia.dao.annotation.DeleteInfo;
import uia.dao.annotation.SelectInfo;
import uia.dao.annotation.UpdateInfo;

@DaoInfo(type = Lookup.class)
public abstract class LookupDao extends TableDao<Lookup> {

    public LookupDao(Connection conn, TableDaoHelper<Lookup> helper) {
        super(conn, helper);
    }

    @SelectInfo(sql = "WHERE id=? ORDER BY sub_id,param_name")
    public abstract List<Lookup> select(String id) throws SQLException, DaoException;

    @SelectInfo(sql = "WHERE id=? AND sub_id=? ORDER BY param_name")
    public abstract List<Lookup> select(String id, String subId) throws SQLException, DaoException;

    @UpdateInfo(sql = "SET param_value=? WHERE id=?")
    public abstract int update(String value, String id) throws SQLException, DaoException;

    @DeleteInfo(sql = "WHERE id=?")
    public abstract int delete(String id) throws SQLException, DaoException;
}
