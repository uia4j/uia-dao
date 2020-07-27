package uia.dao.pms;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import uia.dao.DaoException;
import uia.dao.ViewDao;
import uia.dao.ViewDaoHelper;
import uia.dao.annotation.DaoInfo;
import uia.dao.annotation.SelectInfo;

@DaoInfo(type = ViewEquip.class)
public abstract class ViewEquipDao extends ViewDao<ViewEquip> {

    public ViewEquipDao(Connection conn, ViewDaoHelper<ViewEquip> helper) {
        super(conn, helper);
    }

    @SelectInfo(sql="WHERE id=?")
    public abstract ViewEquip selectByPK(String id) throws SQLException, DaoException;
    
    @SelectInfo(sql="WHERE equip_name=?")
    public abstract ViewEquip selectByName(String equipName) throws SQLException, DaoException;
    
    @SelectInfo(sql="WHERE equip_group_name is not null ORDER BY equip_name")
    public abstract List<ViewEquip> selectWithGroup() throws SQLException, DaoException;

    @SelectInfo(sql="WHERE equip_group_id=? ORDER BY equip_name")
    public abstract List<ViewEquip> selectByGroup(String equipGroupId) throws SQLException, DaoException;}
