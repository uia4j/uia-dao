DAO Simple Solution
================
The main purpose of the API is to implement DAO pattern simplify using `java.sql` package.

There are three topics:
* DTO File Generator
* DAO Pattern
* Statement Builder

Datebases the API supports are:
* PostgreSQL
* Oracle
* HANA

### Maven
```xml
<dependency>
	<groupId>org.uia.solution</groupId>
	<artifactId>uia-dao</artifactId>
	<version>0.2.1</version>
</dependency>
```

## DAO Pattern

The DAO pattern accesses database using `java.sql` package. You can use pre-implemtated DAO class or inherit it and use `java.sql.Connection` for full control.

* Focus on the design of DTO classes without any XML file.
* Built-in CRUD SQL statements based on annotations in the DTO class.
* No need to implement the standard CRUD methods.
* Minimum implementation, maximum functionality.

### Key Points
#### classes
* TableDao<T> - The generic DAO for a table.
* ViewDao<T> - The generic DAO for a view.
* TableDaoHelper<T> - The DAO helper for a table.
* ViewDaoHelper<T> - The DAO helper for a view.

#### annotations
* TableInfo - annotate a class for a table.
* ViewInfo - annotate a class for a view.
* ColumnInfo - annotate attributes for the columns.

### How To Use
1. Define a DTO for a table.

    ```java
    package a.b.c;

    @TableName(name = "job_detail")
    public class JobDetail {

        @ColumnName(name = "id", primaryKey = true)
        private Stirng id;

        @ColumnName(name = "job_id")
        private String jobId;

        @ColumnName(name = "job_detail_name")
        private String jobDetailName;
    }
    ```

2. Define a DTO for a view.
    Setup __inherit__ levels if the class inherits from some class.

    ```java
    package a.b.c;

    @ViewName(name = "view_job_detail", inherit = 1)
    public class ViewJobDetail extends JobDetail {

        @ColumnName(name = "job_name")
        private String jobName;
    }
    ```

3. Creat a fatory and load definition of DTO classes.

    ```java
    DaoFactory factory = new DaoFactory();
    factory.load("a.b.c");
    ```

4. Run __CRUD__ on a table

    ```java
    TableDao<JobDetail> dao = new TableDao(conn, factory.forTable(JobDetail.class));
    dao.insert(...);
    dao.udpate(...);
    dao.deleteByPK(...);
    List<JobDetail> result = dao.selectAll();
    JobDetail one = dao.selectByPK(...);
    ```

3. Run a __SELECT__ on a view

    ```java
    ViewDao<ViewJobDetail> dao = new ViewDao(conn, factory.forView(ViewJobDetail.class));
    List<ViewJobDetail> result = dao.selectAll();
    ```

### Custom DAO
Use simple SQL statements instead of writing __Spaghetti SQL__.

#### Inherit from uia.dao.TableDao to access a table

```java
public class JobDetailDao exttends TableDao<JobDetail> {

    public JobDetailDetail(Connection conn) {
        super(conn, factory.forTable(JobDetail.class));
    }

    public List<JobDtail> selectByName(String name) {
        // Get the SELECT method
        DaoMethod<JobDetail> method = this.tableHelper.forSelect();

        // Prepare a statment with custom WHERE criteria.
        try (PreparedStatement ps = this.conn.prepareStatement(method.getSql() + "WHERE job_detail_name like ?")) {
            ps.setString(1, name);

            // Execute
            try (ResultSet rs = ps.executeQuery()) {
                // Convert result to DTO object list
                return method.toList(rs);
            }
        }
    }
}
```

#### Inherit from uia.dao.ViewDao for access a view

```java
public class ViewJobDetailDao exttends ViewTableDao<ViewJobDetail> {

    public ViewJobDetailDetail(Connection conn) {
        super(conn, factory.forTable(ViewJobDetail.class));
    }

    public List<ViewJobDetail> selectByName(String name) {
        // Prepare a statment with custom WHERE criteria.
        try (PreparedStatement ps = this.conn.prepareStatement(getSql() + "WHERE job_detail_name like ?")) {
            ps.setString(1, name);

            // Execute
            try (ResultSet rs = ps.executeQuery()) {
                return toList(rs);
            }
        }
    }
}
```

## Statement Builder

### AND
Example: *c1=? __and__ (c2 between ? and ?) __and__ c3 like ? __and__ c4<>?*
```java
SimpleWhere and = Where.simpleAnd()
    .eq("c1", "abc")
    .between("c2", "123", "456")
    .likeBegin("c3", "abc")
    .notEq("c4", "def");
```

### OR
Example: *c1=? __or__ (c2 between ? and ?) __or__ c3 like ? __or__ c4<>?*
```java
SimpleWhere or = Where.simpleOr()
    .eqOrNull("c1", "abc")
    .between("c2", "123", "456")
    .likeBeginOrNull("c3", "abc")
    .notEq("c4", "def");
```

### AND + OR
Example #1: *(A=? __and__ B=?) __or__ (C=? __and__ D=?)*
```java
SimpleWhere and1 = Where.simpleAnd()
        .eq("A", "A1")
        .eq("B", "B1");

SimpleWhere and2 = Where.simpleAnd()
        .eq("C", "C1")
        .eq("D", "D1");

WhereOr where = Where.or(and1, and2);
```

Example #2: *(A=? __or__ B=?) __and__ (C=? __or__ D=?)*
```java
SimpleWhere or1 = Where.simpleOr()
        .eq("A", "A1")
        .eq("B", "B1");

SimpleWhere or2 = Where.simpleOr()
        .eq("C", "C1")
        .eq("D", "D1");

WhereAnd where = Where.and(or1, or2);
```

### Select Statement
Create a __READY TO EXECUTE__ `PreparedStatement` object.

Example: __*SELECT id,revision,sch_name FROM pms_schedule WHERE state_name=? ORDER BY id*__
```java
// where
SimpleWhere where = Where.simpleAnd()
        .notEq("state_name", "on");

// select
SelectStatement select = new SelectStatement("SELECT id,revision,sch_name FROM pms_schedule")
    	.where(where)
    	.orderBy("id");
try (PreparedStatement ps = select.prepare(conn)) {
    try (ResultSet rs = ps.executeQuery()) {
    	while (rs.next()) {
        	System.out.println(rs.getObject(3));
    	}
    }
}
```

## DTO File Generator
The tool can generate the Java file based on table and view schema in the database.

```java
String dtoPackage = "a.b.c";                        // package name
String tableName = "job_dtail";                     // table name
String viewName = "view_job_dtail";                 // view name

// PostgreSQL
Database db = new PostgreSQL(host, port, dbName, user, password);

// output stirng only
String clzTable = DaoFactoryClassPrinter(db, tableName).generateDTO(
        dtoPackage,
        CamelNaming.upper(tableName));
String clzView = DaoFactoryClassPrinter(db, viewName).generateDTO(
        dtoPackage,
        CamelNaming.upper(tableName));

// save to files
String sourceDir = "d:/my_project/src/main/java";   // save path
DaoFactoryTool tool = new DaoFactoryTool(db);
tool.toDTO(sourceDir, dtoPackage, tableName)
tool.toDTO(sourceDir, dtoPackage, tableName)
```
