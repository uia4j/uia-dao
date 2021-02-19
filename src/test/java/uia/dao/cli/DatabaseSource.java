package uia.dao.cli;

import java.sql.SQLException;

import uia.dao.Database;
import uia.dao.hana.Hana;
import uia.dao.ora.Oracle;
import uia.dao.pg.PostgreSQL;

public abstract class DatabaseSource {

    public static Database byType(String name) throws SQLException {
        if ("pg".equalsIgnoreCase(name)) {
            PostgreSQL pg = new PostgreSQL();
            pg.setAlwaysTimestampZ(true);
            return pg;
        }
        if ("ora".equalsIgnoreCase(name)) {
            Oracle ora = new Oracle();
            ora.setAlwaysNVarchar(true);
            return ora;
        }
        if ("hana".equalsIgnoreCase(name)) {
            return new Hana();
        }

        throw new SQLException("no datasource: " + name);
    }

    public static Database byReal(String name) throws SQLException {
        if ("wip".equalsIgnoreCase(name)) {
            PostgreSQL pg = new PostgreSQL("localhost", "5432", "wipdb", "wip", "wip", "public");
            pg.setAlwaysTimestampZ(true);
            return pg;
        }
        if ("pgsvr96".equalsIgnoreCase(name)) {
            PostgreSQL pg = new PostgreSQL("localhost", "5432", "pmsdb", "pms", "pms", "public");
            pg.setAlwaysTimestampZ(true);
            return pg;
        }
        if ("pgsvr96_0713".equalsIgnoreCase(name)) {
            PostgreSQL pg = new PostgreSQL("localhost", "5432", "pmsdb_0713", "pms", "pms", "public");
            pg.setAlwaysTimestampZ(true);
            return pg;
        }
        if ("pgsvr96_tci".equalsIgnoreCase(name)) {
            PostgreSQL pg = new PostgreSQL("localhost", "5432", "pmsdb_tci", "pms", "pms", "public");
            pg.setAlwaysTimestampZ(true);
            return pg;
        }
        if ("orasvr12".equalsIgnoreCase(name)) {
            Oracle ora = new Oracle("localhost", "1521", "ORCLCDB.localdomain", "PMS", "PMS");
            ora.setAlwaysNVarchar(true);
            return ora;
        }
        if ("aliyun".equalsIgnoreCase(name)) {
            Hana hana = new Hana("47.103.38.215", "39044", "PMS", "PMS", "PMS#123win");
            hana.setAlwaysNVarchar(true);
            return hana;
        }
        if ("aliyun_wip".equalsIgnoreCase(name)) {
            Hana hana = new Hana("47.103.38.215", "39044", "WIP", "WIP", "MES#123win");
            hana.setAlwaysNVarchar(true);
            return hana;
        }
        if ("tym_pms".equalsIgnoreCase(name)) {
            Hana hana = new Hana("10.135.236.52", "39015", "PMS", "PMS", "PMSpms");
            hana.setAlwaysNVarchar(true);
            return hana;
        }
        if ("tym_wip".equalsIgnoreCase(name)) {
            Hana hana = new Hana("10.135.236.52", "39015", "WIP", "MES", "mes123");
            hana.setAlwaysNVarchar(true);
            return hana;
        }
        if ("kao_pms".equalsIgnoreCase(name)) {
            Hana hana = new Hana("192.168.137.245", "39015", "PMS", "PMS", "PMSpms");
            hana.setAlwaysNVarchar(true);
            return hana;
        }
        if ("scm".equalsIgnoreCase(name)) {
            PostgreSQL pg = new PostgreSQL("localhost", "5432", "scmdb", "scm", "scmAdmin");
            pg.setAlwaysNVarchar(true);
            return pg;
        }
        if ("scm_fmtest".equalsIgnoreCase(name)) {
            PostgreSQL pg = new PostgreSQL("10.10.2.169", "5432", "scmdb", "scm", "scmAdmin");
            pg.setAlwaysNVarchar(true);
            return pg;
        }
        if ("htks_prod".equalsIgnoreCase(name)) {
            Hana hana = new Hana("10.160.2.20", "30015", "WIP", "WIP", "Sap12345");
            hana.setAlwaysNVarchar(true);
            return hana;
        }
        if ("htks_test_arch".equalsIgnoreCase(name)) {
            Hana hana = new Hana("10.160.2.23", "31015", "WIP_ARCHIVE", "WIP_ARCHIVE", "Sap12345");
            hana.setAlwaysNVarchar(true);
            return hana;
        }
        if ("zztop".equalsIgnoreCase(name)) {
            PostgreSQL pg = new PostgreSQL("localhost", "5432", "pmsdb", "pms", "pms", "zztop");
            pg.setAlwaysTimestampZ(true);
            return pg;
        }
        if ("scm_po".equalsIgnoreCase(name)) {
            PostgreSQL pg = new PostgreSQL("localhost", "5432", "scmdb", "scm", "scmAdmin", "po");
            pg.setAlwaysNVarchar(true);
            return pg;
        }
        if ("scm_po_fmtest".equalsIgnoreCase(name)) {
            PostgreSQL pg = new PostgreSQL("10.10.2.169", "5432", "scmdb", "scm", "scmAdmin", "po");
            pg.setAlwaysNVarchar(true);
            return pg;
        }
        if ("yo_hmd".equalsIgnoreCase(name)) {
        	Hana pg = new Hana("10.3.11.37", "39044", "WIP", "WIP", "wip123");
            pg.setAlwaysTimestampZ(true);
            return pg;
        }
        if ("yo_hmq".equalsIgnoreCase(name)) {
        	Hana pg = new Hana("10.3.11.37", "39041", "WIP", "WIP", "wip123");
            pg.setAlwaysTimestampZ(true);
            return pg;
        }
        if ("pms_yo_hmd".equalsIgnoreCase(name)) {
        	Hana pg = new Hana("10.3.11.37", "39044", "PMS", "PMS", "Sap12345");
            pg.setAlwaysTimestampZ(true);
            return pg;
        }
        if ("pms_yo_hmq".equalsIgnoreCase(name)) {
        	Hana pg = new Hana("10.3.11.37", "39041", "PMS", "PMS", "Sap12345");
            pg.setAlwaysTimestampZ(true);
            return pg;
        }
        if ("pms_yo_hmp".equalsIgnoreCase(name)) {
        	Hana pg = new Hana("10.3.11.34", "30015", "PMS", "PMS", "PMS@zaq1");
            pg.setAlwaysTimestampZ(true);
            return pg;
        }
        if ("pms_yo_hms".equalsIgnoreCase(name)) {
        	Hana pg = new Hana("10.3.11.34", "30041", "PMS", "PMS", "PMS@zaq1");
            pg.setAlwaysTimestampZ(true);
            return pg;
        }

        throw new SQLException("no datasource: " + name);
    }
}
