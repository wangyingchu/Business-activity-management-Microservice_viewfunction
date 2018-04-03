package com.viewfunction.messageEngine.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import com.viewfunction.messageEngine.exception.MessageEngineException;

public class PerportyHandler {
	private static Properties _properties;		
	public static String AMQP_VIRTUALHOST="AMQP_VIRTUALHOST";
	public static String AMQP_USERNAME="AMQP_USERNAME";
	public static String AMQP_USERPWD="AMQP_USERPWD";
	public static String AMQP_CLIENTID="AMQP_CLIENTID";
	public static String AMQP_BROKERLIST="AMQP_BROKERLIST";
	
	public static String getPerportyValue(String resourceFileName) throws MessageEngineException{		
		_properties=new Properties();
		try {
			_properties.load(new FileInputStream(RuntimeEnvironmentHandler.getApplicationRootPath()+"CentralMessageEngineCfg.properties"));
		} catch (FileNotFoundException e) {
			MessageEngineException cpe=new MessageEngineException();
			cpe.initCause(e);
			throw cpe;
		} catch (IOException e) {
			MessageEngineException cpe=new MessageEngineException();
			cpe.initCause(e);
			throw cpe;
		}		
		return _properties.getProperty(resourceFileName);
	}
	
	public static void main(String[] args) throws MessageEngineException{
		System.out.println(getPerportyValue(PerportyHandler.AMQP_USERNAME));		
	}
}
