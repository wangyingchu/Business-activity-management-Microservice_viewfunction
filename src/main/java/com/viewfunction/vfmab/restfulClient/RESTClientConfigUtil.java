package com.viewfunction.vfmab.restfulClient;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class RESTClientConfigUtil {
	private static Properties _properties;
	
	public static String getREST_baseURLValue(){		
		if(_properties==null){
			_properties=new Properties();
			try {			
				String configFileLocation=RESTClientConfigUtil.class.getResource("/").getPath().toString()+"VFBAM_serviceApplicationRESTClient.properties";				
				_properties.load(new FileInputStream(configFileLocation));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return _properties.getProperty("VFBAM_ServiceApplicationREST_baseURL");
	}
}