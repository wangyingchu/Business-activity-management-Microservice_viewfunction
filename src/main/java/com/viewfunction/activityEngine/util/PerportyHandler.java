package com.viewfunction.activityEngine.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import com.viewfunction.contentRepository.util.RuntimeEnvironmentHandler;

public class PerportyHandler {
	private static Properties _properties;		
	public static String SPACEEVENT_LISTENERSLOADER_IMPL_CLASSNAME="SPACEEVENT_LISTENERSLOADER_IMPL_CLASSNAME";
	public static String getPerportyValue(String resourceFileName){		
		_properties=new Properties();
		try {
			_properties.load(new FileInputStream(RuntimeEnvironmentHandler.getApplicationRootPath()+"CentralActivityEngineCfg.properties"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return _properties.getProperty(resourceFileName);
	}
	
	public static void main(String[] args){
		System.out.println(getPerportyValue(PerportyHandler.SPACEEVENT_LISTENERSLOADER_IMPL_CLASSNAME));		
	}
}
