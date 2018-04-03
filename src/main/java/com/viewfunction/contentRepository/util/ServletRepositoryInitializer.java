package com.viewfunction.contentRepository.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.servlet.ServletException;

import org.apache.jackrabbit.api.JackrabbitRepository;
import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.apache.jackrabbit.servlet.AbstractRepositoryServlet;

import com.viewfunction.contentRepository.util.exception.ContentReposityRuntimeException;
import com.viewfunction.contentRepository.util.factory.ContentComponentFactory;

public class ServletRepositoryInitializer extends AbstractRepositoryServlet {

	private static final long serialVersionUID = 1183144091180543327L;

	private JackrabbitRepository repository;

	public void init() throws ServletException {
/*
		try {
			
			String repositoryHome=RuntimeEnvironmentHandler.getApplicationRootPath()+"contentRepository";
			
			File home = new File(repositoryHome);
			if (!home.exists()) {
				log("Creating repository home directory: " + home);
				home.mkdirs();
			}

			String configFile="";			
			String dataPersistenceType;
			try {
				dataPersistenceType = PerportyHandler.getPerportyValue(PerportyHandler.DATA_PERSISTENCE_TYPE);
				if(dataPersistenceType.equalsIgnoreCase(PerportyHandler.DATA_PERSISTENCE_BUILDIN)){
					configFile= RuntimeEnvironmentHandler.getApplicationRootPath()+"repository_BuildinDerbyPersistence.xml";		            
				}else if(dataPersistenceType.equalsIgnoreCase(PerportyHandler.DATA_PERSISTENCE_MYSQL)){
					configFile= RuntimeEnvironmentHandler.getApplicationRootPath()+"repository_MySQLPersistence.xml";  
				}	
				
			} catch (ContentReposityRuntimeException e) {				
				e.printStackTrace();
			}				
			
			File config = new File(configFile);			
			if (!config.exists()) {
				log("Creating default repository configuration: " + config);
				createDefaultConfiguration(config);
			}			
			
			repository = RepositoryImpl.create(RepositoryConfig.create(config.toURI(), home.getPath()));			
			ContentComponentFactory.setRepository(repository);		
			
		} catch (RepositoryException e) {
			throw new ServletException("Failed to start Jackrabbit", e);
		}
*/
		super.init();
	}

	public void destroy() {
		super.destroy();
		/*
		repository.shutdown();
		*/
	}

	private void createDefaultConfiguration(File config)throws ServletException {
		try {
			OutputStream output = new FileOutputStream(config);
			try {
				InputStream input = RepositoryImpl.class.getResourceAsStream("repository.xml");
				try {
					byte[] buffer = new byte[8192];
					int n = input.read(buffer);
					while (n != -1) {
						output.write(buffer, 0, n);
						n = input.read(buffer);
					}
				} finally {
					input.close();
				}
			} finally {
				output.close();
			}
		} catch (IOException e) {
			throw new ServletException("Failed to copy default configuration: "	+ config, e);
		}
	}

	@Override
	protected Repository getRepository() throws RepositoryException {
		return repository;
	}
}