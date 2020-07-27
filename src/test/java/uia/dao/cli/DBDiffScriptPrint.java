package uia.dao.cli;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import uia.dao.ComparePlan;
import uia.dao.CompareResult;
import uia.dao.Database;
import uia.dao.TableType;

public class DBDiffScriptPrint extends AbstractCmd {

    @Test
    public void test() throws Exception {
        //String[] tables = new String[] {
        //"SHOP_ORDER",
        //"ACTIVITY_LOG",
        //"SHOP_ORDER_SFC",
        //"SFC",
        //"SFC_ID_HISTORY",
        //"SFC_BOM",
        //"SFC_ROUTING",
        //"SFC_ROUTER",
        //"SFC_STEP",
        //"HOLD_DETAIL",
        //"ZD_SFC",
        //"ZR_BINMAP_COMBINE",
        //"ZR_DC_RESULT",
        //"ZR_DC_RESULT_DETAIL",
        //"ZR_DC_RESULT_RAW",
        //"ZR_DC_RESULT_STAT",
        //"ZR_DISPATCH_SFC",
        //"ZR_RUN_DEFECT",
        //"ZR_RUN_JUMP",
        //"ZR_RUN_MT_SFC",
        //"ZR_RUN_SFC",
        //"ZR_RUN_SPLIT_MERGE",
        //"ZR_SAMPLING_RESULT",
        //"ZR_SFC_LOG",
        //"ZR_SPC_ALARM",
        //"ZR_SPC_ALARM_DATA",
        //"ZR_SPC_ALARM_DATA_MASTER",
        //"ZR_SPC_ALARM_MAIL",
        //"ZR_HOLD_RELEASE_SFC",
        //"ZR_HOLD_RELEASE_SFC_ITEM",
        //"ZR_HOLD_RELEASE_SFC_ITEM_STATE",
        //"ZR_SFC_MEMO",
        //"ZD_SHOP_ORDER_ITEMSET",
        //"ZD_CUSTOMER_ITEM",
        //"ZD_SFC_ITEM",
        //"ZD_SFC_ITEM_GRADE",
        //"ZD_LOOKUP_EX",
        //"ZR_CUSTOMER_ITEM_DEFECT",
        //"ZR_CUSTOMER_ITEM_GRADE",
        //"ZR_CUSTOMER_ITEM_OPERATION_DEFECT",
        //"ZR_CUSTOMER_ITEM_OPERATION_YIELD",
        //"ZR_SAMPLING_RESULT_DETAIL",
        //"ZR_RUN_SFC_ITEM",
        //"ZR_RUN_SPLIT_MERGE_ITEM",
        //"ZR_SHOP_ORDER_ITEMSET_LOG",
        //"ZR_ROUTE_EVENT",
        //"ZR_PKG_CONTAINER",
        //"ZR_PKG_CONTAINER_LOG",
        //"ZR_PKG_CONTAINER_LABEL",
        //"ZR_PKG_CONTAINER_SFC",
        //"ZR_PKG_CONTAINER_SFC_LOG"
        //};

        execute(new String[] {
                "-s", "pgsvr96",
                "-t", "aliyun",
                "--table", null,
                "--view", null,
                "--dbtype", "hana" });
    }

    public void execute(String[] args) throws Exception {
        Database source = byReal(args[1]);
        Database target = byReal(args[3]);
        Database dbtype = byType(args[9]);

        // table
        List<String> tns = args[5] != null ? Arrays.asList(args[5].split(",")) : source.selectTableNames();
        for (String tn : tns) {
            TableType ta = source.selectTable(tn, false);
            TableType tb = target.selectTable(tn, false);
            if(tb == null) {
                System.out.println("-- " + tn + ", " + ta.getColumns().size() + "/0");
            }
            else {
                System.out.println("-- " + tn + ", " + ta.getColumns().size() + "/" + tb.getColumns().size());
            }

            CompareResult cr = ta.sameAs(tb, new ComparePlan(false, true, true, true, true));
            if (!cr.isPassed()) {
                if (cr.isMissing()) {
                    System.out.println(dbtype.generateCreateTableSQL(ta));
                }
                else {
                    System.out.println(dbtype.generateAlterTableSQL(tn, cr.getDiff()));
                }
            }
        }

        // view
        List<String> vns = args[7] != null ? Arrays.asList(args[7].split(",")) : source.selectViewNames();
        for (String vn : vns) {
            TableType va = source.selectTable(vn, false);
            TableType vb = target.selectTable(vn, false);
            CompareResult cr = va.sameAs(vb, ComparePlan.view());

            System.out.println("-- " + vn);
            if (!cr.isPassed()) {
                if (!cr.isMissing()) {
                    System.out.println(dbtype.generateDropViewSQL(vn) + ";");
                }
                System.out.println(dbtype.generateCreateViewSQL(vn, source.selectViewScript(vn)) + ";");
                System.out.println();
            }
        }
    }
}
