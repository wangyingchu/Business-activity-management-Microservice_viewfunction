package com.viewfunction.contentRepository.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import com.viewfunction.contentRepository.util.exception.ContentReposityRuntimeException;

public class PerportyHandler {

	private static Properties _properties;		
	public static String USER_AUTHENTICATION_METHOD="USER_AUTHENTICATION_METHOD";
	public static String BUILDIN_ADMINISTRATOR_ACCOUNT="BUILDIN_ADMINISTRATOR_ACCOUNT";
	public static String BUILDIN_ADMINISTRATOR_ACCOUNT_PWD="BUILDIN_ADMINISTRATOR_ACCOUNT_PWD";
	public static String USER_AUTHENTICATION_LDAP_SERVER_ADDRESS="USER_AUTHENTICATION_LDAP_SERVER_ADDRESS";
	public static String USER_AUTHENTICATION_LDAP_SERVICE_PORT="USER_AUTHENTICATION_LDAP_SERVICE_PORT";
	public static String MONGODB_SERVER_ADDRESS="MONGODB_SERVER_ADDRESS";
	public static String MONGODB_SERVER_PORT="MONGODB_SERVER_PORT";
	public static String ENABLE_GLOBAL_FULLTEXT_SEARCH="ENABLE_GLOBAL_FULLTEXT_SEARCH";
	
	public static String getPerportyValue(String resourceFileName) throws ContentReposityRuntimeException{		
		_properties=new Properties();
		try {
			_properties.load(new FileInputStream(RuntimeEnvironmentHandler.getApplicationRootPath()+"CentralContentRepositoryCfg.properties"));
		} catch (FileNotFoundException e) {
			ContentReposityRuntimeException cpe=new ContentReposityRuntimeException();
			cpe.initCause(e);
			throw cpe;
		} catch (IOException e) {
			ContentReposityRuntimeException cpe=new ContentReposityRuntimeException();
			cpe.initCause(e);
			throw cpe;
		}		
		return _properties.getProperty(resourceFileName);
	}
	
	public static void main(String[] args) throws ContentReposityRuntimeException{
		System.out.println(getPerportyValue(PerportyHandler.USER_AUTHENTICATION_METHOD));		
	}
			
	
	
}
