package com.viewfunction.activitySpaceEventListeners;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class PropertiesConfigUtil {
	private static Properties _properties;
	
	public static String getPropertyValue(String propertyName){
		if(_properties==null){
			_properties=new Properties();
			try {			
				String configFileLocation=PropertiesConfigUtil.class.getResource("/").getPath().toString()+"ActivitySpaceEventListenersConfig.properties";				
				_properties.load(new FileInputStream(configFileLocation));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return _properties.getProperty(propertyName);
	}
}
