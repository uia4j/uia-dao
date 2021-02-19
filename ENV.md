DaoEnv How To
================

## Description

`uia.dao.DaoEnv` is a abstract class which try to use a simpler way to access database.

DaoEnv provides 5 helper methods to create the DaoEnv object. 
* DaoEnv.dataSource - Connection from data source.
* DaoEnv.pool - Connection from connection pool. The implementation uses `Hikari`.
* DaoEnv.postgre - Simple PostgreSQL JDBC connection.
* DaoEnv.hana - Simple HANA JDBC connection.
* DaoEnv.oracle - Simple Oracle JDBC connection.

All helper methods above have two arguments
* date2UTC - if timestamp needs to be converted to UTC time.
* packageName - The package name of DTO classes.
  

## How To

### 1. Create a DaoEnv object.
* Data Source

    ```java
    DaoEnv env = DaoEnv
        .dataSource(false, "your.dto.package")
        .config(connString, user, password, schema);
    ```

* Connection Pool

    ```java
    DaoEnv env = DaoEnv
        .pool(false, "your.dto.package")
        .config(connString, user, password, schema);
    ```

* HANA

    ```java
    DaoEnv env = DaoEnv
        .hana(false, "your.dto.package")
        .config(connString, user, password, schema);
    ```

* Oracle

    ```java
    DaoEnv env = DaoEnv
        .oracle(false, "your.dto.package")
        .config(connString, user, password, schema);
    ```

* PostgreSQL

    ```java
    DaoEnv env = DaoEnv
        .postgre(false, "your.dto.package")
        .config(connString, user, password, schema);
    ```

### 2. Use a session to create a Dao object and select all data.
* Use custom DAO object

    ```java
    public class MyClassDao extends TableDao<MyClass> {
        ...
    }
    ```

    ```java
    try (DaoSession session = env.createSession()) {
        MyClassDao dao = session.tableDao(MyClassDao.class);
        List<MyClass> result = dao.selectAll();
    }
    ```

* Use generic `uia.dao.TableDao` object

    ```java
    try (DaoSession session = env.createSession()) {
        TableDao<MyClass> dao = session.forTable(MyClass.class);
        List<MyClass> result = dao.selectAll();
    }
    ```

## Spring Integration

### 1. Create a component 

The component inherits from `uia.dao.DaoEnv` and annotate scope to `ConfigurableBeanFactory.SCOPE_SINGLETON`.

```java
package a.b.c;

@Component("db")
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class MyDaoEnv extends DaoEnv {
	
	public MyDaoEnv() throws Exception {
		super(DaoEnv.POSTGRE, false);   // connect to the PostgreSQL
		config(connString, user, password, null);
	}

	@Override
	protected void initialFactory(DaoFactory factory) throws Exception {
		factory.load("your.dto.package");
	}

```

### 2. Create a configuration class

```java
package a.b.c;

@Configuration
@ComponentScan
public class DaoEnvConfig {
	
}
```


### 3. use `@Autowired` to get the `DaoEnv` instance.
###
```java
@Service
public class MyServiceImpl implements MyService {

	@Autowired
	@Qualifier("db")
	private DaoEnv env; // Component: "db" 
	
	@Override
	public void run() throws Exception {
		try(DaoSession session = env.createSession()) {
			...
		}
	}
}
```
