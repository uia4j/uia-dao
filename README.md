DAO Simple Solution
================
## Description

The main purpose of the API is to implement DAO pattern that uses `java.sql` package to access database.

There are three topics:
* DTO File Generator
* DAO Pattern
* Statement Builder

Databases the API supports are:
* PostgreSQL
* Oracle
* SQLServer
* HANA

Maven
```xml
<dependency>
	<groupId>org.uia.solution</groupId>
	<artifactId>uia-dao</artifactId>
	<version>0.3.0</version>
</dependency>
```

Other topics

  * [DaoEnv](ENV.md)
  * [SpringFramework](ENV.md)
  

## Core Concept

The implementation of the API uses `java.sql` package tos access the database. You can use pre-implemented `Dao` class or inherit it to provide meaningful methods to CRUD data.

When you use the API, you can

* Use annotations to design DTO classes without any XML file.
* No need to implement the standard CRUD methods.
* Minimum implementation, maximum functionality.

### Key Points
#### classes
* TableDao<T> - The generic DAO to access a table.
* ViewDao<T> - The generic DAO to access a view.
* TableDaoHelper<T> - The DAO helper for a table.
* ViewDaoHelper<T> - The DAO helper for a view.

#### annotations
* TableInfo - annotate a class for a table.
* ViewInfo - annotate a class for a view.
* ColumnInfo - annotate attributes for the columns.
* SelectInfo - annotate methods to select data.
* UpdateInfo - annotate methods to update data.
* DeleteInfo - annotate methods to delete data.

### How To Use
1. Define a DTO for a table.

    ```java
    package a.b.c;

    @TableInfo(name = "job_detail")
    public class JobDetail {

        @ColumnInfo(name = "id", primaryKey = true)
        private String id;

        @ColumnInfo(name = "job_id")
        private String jobId;

        @ColumnInfo(name = "job_detail_name")
        private String jobDetailName;
    }
    ```

2. Define a DTO for a view.
    Setup __inherit__ levels if the class inherits from some class.

    ```java
    package a.b.c;

    @ViewInfo(name = "view_job_detail")
    public class ViewJobDetail extends JobDetail {

        @ColumnInfo(name = "job_name")
        private String jobName;
    }
    ```

3. Create a factory and load definition of DTO classes.

    ```java
    DaoFactory factory = new DaoFactory();
    factory.load("a.b.c");
    ```

4. Run __CRUD__ on a table
    ```java
    // create a dao object
    TableDao<JobDetail> dao = new TableDao(
            conn, 
            factory.forTable(JobDetail.class));
    dao.insert(...);
    dao.update(...);
    dao.deleteByPK(...);
    List<JobDetail> result = dao.selectAll();
    JobDetail one = dao.selectByPK(...);
    ```
    or
    ```java
    // create a dao object
    TableDao<JobDetail> dao = factory.createTableDao(
            JobDetail.class,
            conn);
    dao.insert(...);
    dao.update(...);
    dao.deleteByPK(...);
    List<JobDetail> result = dao.selectAll();
    JobDetail one = dao.selectByPK(...);
    ```

5. Run a __SELECT__ on a view
    ```java
    // create a dao object
    ViewDao<ViewJobDetail> dao = new ViewDao(
            conn, 
            factory.forView(ViewJobDetail.class));
    List<ViewJobDetail> result = dao.selectAll();
    ```
    or
    ```java
    // create a dao object
    ViewDao<ViewJobDetail> dao = factory.createViewDao(
            ViewJobDetail.class,
            conn);
    List<ViewJobDetail> result = dao.selectAll();
    ```


### Custom DAO
Use simple SQL statements instead of writing __Spaghetti SQL__.

#### Inherit from uia.dao.TableDao to access a table

```java
public class JobDetailDao extends TableDao<JobDetail> {

    public JobDetailDetail(Connection conn) {
        super(conn, factory.forTable(JobDetail.class));
    }

    public List<JobDetail> selectByName(String name) {
        // Get the SELECT method
        DaoMethod<JobDetail> method = this.tableHelper.forSelect();

        // Prepare a statement with custom WHERE criteria.
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
public class ViewJobDetailDao extends ViewTableDao<ViewJobDetail> {

    public ViewJobDetailDetail(Connection conn) {
        super(conn, factory.forTable(ViewJobDetail.class));
    }

    public List<ViewJobDetail> selectByName(String name) {
        // Prepare a statement with custom WHERE criteria.
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

### SelectStatement
Used to create a READY TO BE EXECUTED `PreparedStatement` object.

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
The tool can generate the Java file based on the table and view schema in the database.

```java
String sourceDir = "d:/my_project/src/main/java";   // save path
String dtoPackage = "a.b.c";                        // package name
String tableName = "job_detail";                    // table name
String viewName = "view_job_detail";                // view name

// PostgreSQL
Database db = new PostgreSQL(host, port, dbName, user, password);

// save to files
DaoFactoryTool tool = new DaoFactoryTool(db);
// save to files: table
tool.toDTO(sourceDir, dtoPackage, tableName)
// save to files: view
tool.toDTO(sourceDir, dtoPackage, viewName)
```
