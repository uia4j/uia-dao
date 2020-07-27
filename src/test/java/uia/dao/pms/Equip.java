package uia.dao.pms;

import java.util.Date;
import java.util.UUID;

import uia.dao.annotation.ColumnInfo;
import uia.dao.annotation.TableInfo;

/**
 * Table or View: equip.
 *
 * @author UIA
 */
@TableInfo(name = "equip", orderBy = "equip_name")
public class Equip {

    public static final String KEY = "equip";

    @ColumnInfo(name = "id", primaryKey = true)
    private String id;

    @ColumnInfo(name = "equip_group_id")
    private String equipGroupId;

    @ColumnInfo(name = "state_name")
    private String stateName;

    @ColumnInfo(name = "sub_state_name")
    private String subStateName;

    @ColumnInfo(name = "updated_time")
    private Date updatedTime;

    @ColumnInfo(name = "updater")
    private String updater;

    @ColumnInfo(name = "mes_status")
    private String mesStatus;

    @ColumnInfo(name = "equip_name")
    private String equipName;

    @ColumnInfo(name = "equip_type")
    private String equipType;

    @ColumnInfo(name = "ma_count")
    private int maCount;

    @ColumnInfo(name = "ma_last_time")
    private Date maLastTime;

    @ColumnInfo(name = "equip_description")
    private String equipDescription;

    @ColumnInfo(name = "dpt_id")
    private String dptId;

    @ColumnInfo(name = "area_id")
    private String areaId;

    @ColumnInfo(name = "spec_no")
    private String specNo;

    @ColumnInfo(name = "manufacturer")
    private String manufacturer;

    /**
     * Constructor.
     *
     */
    public Equip() {
        this.id = "EQ:" + UUID.randomUUID().toString();
        this.stateName = "on";
        this.subStateName = "on";
        this.updatedTime = new Date();
        this.updater = "PMS";
    }

    /**
     * Constructor.
     *
     * @param data A instance to be cloned.
     */
    public Equip(Equip data) {
        this.id = data.id;
        this.equipGroupId = data.equipGroupId;
        this.stateName = data.stateName;
        this.subStateName = data.subStateName;
        this.updatedTime = data.updatedTime;
        this.updater = data.updater;
        this.mesStatus = data.mesStatus;
        this.equipName = data.equipName;
        this.equipType = data.equipType;
        this.maCount = data.maCount;
        this.maLastTime = data.maLastTime;
        this.equipDescription = data.equipDescription;
        this.dptId = data.dptId;
        this.areaId = data.areaId;
        this.specNo = data.specNo;
        this.manufacturer = data.manufacturer;

    }

    /**
     * Returns 主鍵.
     *
     * @return 主鍵.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Sets 主鍵.
     *
     * @param id 主鍵.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns 設備群組主鍵.
     *
     * @return 設備群組主鍵.
     */
    public String getEquipGroupId() {
        return this.equipGroupId;
    }

    /**
     * Sets 設備群組主鍵.
     *
     * @param equipGroupId 設備群組主鍵.
     */
    public void setEquipGroupId(String equipGroupId) {
        this.equipGroupId = equipGroupId;
    }

    /**
     * Returns 狀態.
     *
     * @return 狀態.
     */
    public String getStateName() {
        return this.stateName;
    }

    /**
     * Sets 狀態.
     *
     * @param stateName 狀態.
     */
    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    /**
     * Returns 子狀態.
     *
     * @return 子狀態.
     */
    public String getSubStateName() {
        return this.subStateName;
    }

    /**
     * Sets 子狀態.
     *
     * @param subStateName 子狀態.
     */
    public void setSubStateName(String subStateName) {
        this.subStateName = subStateName;
    }

    /**
     * Returns 更新時間.
     *
     * @return 更新時間.
     */
    public Date getUpdatedTime() {
        return this.updatedTime == null ? null : new Date(this.updatedTime.getTime());
    }

    /**
     * Sets 更新時間.
     *
     * @param updatedTime 更新時間.
     */
    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime == null ? null : new Date(updatedTime.getTime());
    }

    /**
     * Returns 更新用戶.
     *
     * @return 更新用戶.
     */
    public String getUpdater() {
        return this.updater;
    }

    /**
     * Sets 更新用戶.
     *
     * @param updater 更新用戶.
     */
    public void setUpdater(String updater) {
        this.updater = updater;
    }

    /**
     * Returns MES 狀態.
     *
     * @return MES 狀態.
     */
    public String getMesStatus() {
        return this.mesStatus;
    }

    /**
     * Sets MES 狀態.
     *
     * @param mesStatus MES 狀態.
     */
    public void setMesStatus(String mesStatus) {
        this.mesStatus = mesStatus;
    }

    /**
     * Returns 設備編號.
     *
     * @return 設備編號.
     */
    public String getEquipName() {
        return this.equipName;
    }

    /**
     * Sets 設備編號.
     *
     * @param equipName 設備編號.
     */
    public void setEquipName(String equipName) {
        this.equipName = equipName;
    }

    /**
     * Returns 設備種類.
     *
     * @return 設備種類.
     */
    public String getEquipType() {
        return this.equipType;
    }

    /**
     * Sets 設備種類.
     *
     * @param equipType 設備種類.
     */
    public void setEquipType(String equipType) {
        this.equipType = equipType;
    }

    /**
     * Returns 保養計量值.
     *
     * @return 保養計量值.
     */
    public int getMaCount() {
        return this.maCount;
    }

    /**
     * Sets 保養計量值.
     *
     * @param maCount 保養計量值.
     */
    public void setMaCount(int maCount) {
        this.maCount = maCount;
    }

    /**
     * Returns 最近保養時間.
     *
     * @return 最近保養時間.
     */
    public Date getMaLastTime() {
        return this.maLastTime == null ? null : new Date(this.maLastTime.getTime());
    }

    /**
     * Sets 最近保養時間.
     *
     * @param maLastTime 最近保養時間.
     */
    public void setMaLastTime(Date maLastTime) {
        this.maLastTime = maLastTime == null ? null : new Date(maLastTime.getTime());
    }

    /**
     * Returns 設備說明.
     *
     * @return 設備說明.
     */
    public String getEquipDescription() {
        return this.equipDescription;
    }

    /**
     * Sets 設備說明.
     *
     * @param equipDescription 設備說明.
     */
    public void setEquipDescription(String equipDescription) {
        this.equipDescription = equipDescription;
    }

    public String getDptId() {
        return this.dptId;
    }

    public void setDptId(String dptId) {
        this.dptId = dptId;
    }

    public String getAreaId() {
        return this.areaId;
    }

    public void setAreaId(String areaId) {
        this.areaId = areaId;
    }

    public String getSpecNo() {
        return this.specNo;
    }

    public void setSpecNo(String specNo) {
        this.specNo = specNo;
    }

    public String getManufacturer() {
        return this.manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    /**
     * Copies this instance.
     *
     * @return A new instance.
     */
    public Equip copy() {
        return new Equip(this);
    }

    @Override
    public String toString() {
        return this.id;
    }
}
