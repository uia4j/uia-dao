package uia.dao.pms;

import uia.dao.annotation.ColumnInfo;
import uia.dao.annotation.ViewInfo;

/**
 * Table or View: view_equip.
 *
 * @author UIA
 */
@ViewInfo(name = "view_equip", orderBy = "equip_name", inherit = 1)
public class ViewEquip extends Equip {

    public static final String KEY = "view_equip";

    @ColumnInfo(name = "equip_group_name")
    private String equipGroupName;

    /**
     * Constructor.
     *
     */
    public ViewEquip() {
    }

    /**
     * Constructor.
     *
     * @param data A instance to be cloned.
     */
    public ViewEquip(ViewEquip data) {
        super(data);
        this.equipGroupName = data.equipGroupName;

    }

    public String getEquipGroupName() {
        return this.equipGroupName;
    }

    public void setEquipGroupName(String equipGroupName) {
        this.equipGroupName = equipGroupName;
    }

    @Override
    public ViewEquip copy() {
        return new ViewEquip(this);
    }
}
