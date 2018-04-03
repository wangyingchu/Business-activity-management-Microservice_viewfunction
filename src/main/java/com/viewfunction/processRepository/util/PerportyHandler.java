package com.viewfunction.processRepository.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import com.viewfunction.processRepository.exception.ProcessRepositoryRuntimeException;

public class PerportyHandler {
	private static Properties _properties;		
	public static String DATA_PERSISTENCE_TYPE="DATA_PERSISTENCE_TYPE";
	public static String DATA_PERSISTENCE_TYPE_DATABASE="DATABASE";
	public static String DATA_PERSISTENCE_TYPE_INMEMERY="IN_MEMORY";
	public static String DATA_PERSISTENCE_TYPE_INFILEDB="IN_FILEDB";
	
	public static String ACTIVITI_databaseSchemaUpdate="ACTIVITI_databaseSchemaUpdate";
	public static String ACTIVITI_jdbcUrl="ACTIVITI_jdbcUrl";
	public static String ACTIVITI_jdbcDriver="ACTIVITI_jdbcDriver";
	public static String ACTIVITI_jdbcUsername="ACTIVITI_jdbcUsername";
	public static String ACTIVITI_jdbcPassword="ACTIVITI_jdbcPassword";
	public static String ACTIVITI_jobExecutorActivate="ACTIVITI_jobExecutorActivate";
	//public static String ACTIVITI_dbCycleUsed="ACTIVITI_dbCycleUsed";
	public static String ACTIVITI_asyncExecutorEnabled="ACTIVITI_jobExecutorActivate";
	public static String ACTIVITI_asyncExecutorActivate="ACTIVITI_jobExecutorActivate";
	public static String ACTIVITI_definitionFlowDiagramFont="ACTIVITI_definitionFlowDiagramFont";
	public static String getPerportyValue(String resourceFileName) throws ProcessRepositoryRuntimeException{		
		_properties=new Properties();
		try {
			_properties.load(new FileInputStream(RuntimeEnvironmentHandler.getApplicationRootPath()+"CentralProcessRepositoryCfg.properties"));
		} catch (FileNotFoundException e) {
			ProcessRepositoryRuntimeException cpe=new ProcessRepositoryRuntimeException();
			cpe.initCause(e);
			throw cpe;
		} catch (IOException e) {
			ProcessRepositoryRuntimeException cpe=new ProcessRepositoryRuntimeException();
			cpe.initCause(e);
			throw cpe;
		}		
		return _properties.getProperty(resourceFileName);
	}
	
	public static void main(String[] args) throws ProcessRepositoryRuntimeException{
		System.out.println(getPerportyValue(PerportyHandler.DATA_PERSISTENCE_TYPE));		
	}
}
