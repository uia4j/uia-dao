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
        if ("pgsvr96_test".equalsIgnoreCase(name)) {
            PostgreSQL pg = new PostgreSQL("localhost", "5432", "pmsdb_test", "pms", "pms", "public");
            pg.setAlwaysTimestampZ(true);
            return pg;
        }
        if ("pgsvr96_v2".equalsIgnoreCase(name)) {
            return new PostgreSQL("localhost", "5432", "pmsdbv2", "pms", "pms", "public");
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

        throw new SQLException("no datasource: " + name);
    }
}
