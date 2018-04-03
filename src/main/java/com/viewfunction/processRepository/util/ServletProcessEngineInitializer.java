package com.viewfunction.processRepository.util;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.ProcessEngines;

import com.viewfunction.processRepository.exception.ProcessRepositoryRuntimeException;

public class ServletProcessEngineInitializer implements ServletContextListener{	
	public void contextDestroyed(ServletContextEvent arg0) {
		ProcessEngines.destroy();		
	}

	public void contextInitialized(ServletContextEvent arg0) {
		ProcessEngineConfiguration _ProcessEngineConfiguration=null;
		try {
			String dataPersistenceType=PerportyHandler.getPerportyValue(PerportyHandler.DATA_PERSISTENCE_TYPE).trim();
			if(dataPersistenceType.equals(PerportyHandler.DATA_PERSISTENCE_TYPE_DATABASE)){
				_ProcessEngineConfiguration=ProcessEngineConfiguration.createStandaloneProcessEngineConfiguration();
				_ProcessEngineConfiguration.setDatabaseSchemaUpdate(PerportyHandler.getPerportyValue(PerportyHandler.ACTIVITI_databaseSchemaUpdate).trim());					
				_ProcessEngineConfiguration.setJdbcDriver(PerportyHandler.getPerportyValue(PerportyHandler.ACTIVITI_jdbcDriver).trim());
				_ProcessEngineConfiguration.setJdbcUrl(PerportyHandler.getPerportyValue(PerportyHandler.ACTIVITI_jdbcUrl).trim());					
				_ProcessEngineConfiguration.setJdbcUsername(PerportyHandler.getPerportyValue(PerportyHandler.ACTIVITI_jdbcUsername).trim());
				if(PerportyHandler.getPerportyValue(PerportyHandler.ACTIVITI_jdbcPassword).trim().equals("NA")){
					_ProcessEngineConfiguration.setJdbcPassword("");						
				}else{
					_ProcessEngineConfiguration.setJdbcPassword(PerportyHandler.getPerportyValue(PerportyHandler.ACTIVITI_jdbcPassword).trim());
				}					
				_ProcessEngineConfiguration.setJobExecutorActivate(Boolean.parseBoolean(PerportyHandler.getPerportyValue(PerportyHandler.DATA_PERSISTENCE_TYPE).trim()));
			}else if(dataPersistenceType.equals(PerportyHandler.DATA_PERSISTENCE_TYPE_INMEMERY)){
				_ProcessEngineConfiguration=ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration();					
			}else if(dataPersistenceType.equals(PerportyHandler.DATA_PERSISTENCE_TYPE_INFILEDB)){					
				String fileDBLocation=RuntimeEnvironmentHandler.getApplicationRootPath()+"processRepository";
				String jdbcURL="jdbc:derby:"+fileDBLocation+";create=true";					
				_ProcessEngineConfiguration=ProcessEngineConfiguration.createStandaloneProcessEngineConfiguration();
				_ProcessEngineConfiguration.setDatabaseSchemaUpdate(PerportyHandler.getPerportyValue(PerportyHandler.ACTIVITI_databaseSchemaUpdate).trim());
				_ProcessEngineConfiguration.setJdbcDriver("org.apache.derby.jdbc.EmbeddedDriver");
				_ProcessEngineConfiguration.setJdbcUrl(jdbcURL);//"jdbc:derby:{fullpath}processRepository;create=true"
				_ProcessEngineConfiguration.setJdbcUsername("cprdb");
				_ProcessEngineConfiguration.setJdbcPassword("wyc");
				_ProcessEngineConfiguration.setJobExecutorActivate(Boolean.parseBoolean(PerportyHandler.getPerportyValue(PerportyHandler.DATA_PERSISTENCE_TYPE).trim()));					
			}else{					
				throw new ProcessRepositoryRuntimeException();
			}			
			ProcessEngines.init();			
		} catch (ProcessRepositoryRuntimeException e) {			
			e.printStackTrace();
		}
	}
}