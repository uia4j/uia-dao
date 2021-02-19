package uia.dao;

public class SimpleDaoEnv extends DaoEnv {
	
	private final String packageName;
	
	public SimpleDaoEnv(String envName, boolean dateToUTC, String packageName) throws Exception {
		super(envName, dateToUTC); 
		this.packageName = packageName;
		
		getDaoFactory().load(packageName);
	}

	@Override
	protected void initialFactory(DaoFactory factory) throws Exception {
		factory.load(this.packageName);
	}

}
