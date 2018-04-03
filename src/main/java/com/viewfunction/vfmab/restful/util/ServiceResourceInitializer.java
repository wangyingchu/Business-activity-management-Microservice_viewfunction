package com.viewfunction.vfmab.restful.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeManager;

import com.viewfunction.activityEngine.util.cache.ActivityEngineCache;
import com.viewfunction.activityEngine.util.cache.ActivityEngineCacheUtil;
import com.viewfunction.activityEngine.util.factory.ActivityComponentFactory;
import com.viewfunction.contentRepository.util.RuntimeEnvironmentHandler;

@WebListener
public class ServiceResourceInitializer implements ServletContextListener{
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
	    if(ServiceResourceHolder.getOfficeManager()!=null){
            ServiceResourceHolder.getOfficeManager().stop();
        }
		if(ServiceResourceHolder.getActivityEngineCache()!=null){
			ServiceResourceHolder.getActivityEngineCache().clearCache();
		}		
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {		
		String documentFormatConvertsConfigFile=RuntimeEnvironmentHandler.getApplicationRootPath()+"DocumentsFormatConvertsCfg.properties";		
		Properties _properties=new Properties();
		try {
			_properties.load(new FileInputStream(documentFormatConvertsConfigFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}				
		String openOfficeHome=_properties.getProperty("OPENOFFICE_INSTALL_LOCATION");		
		OfficeManager officeManager=null;	
		DefaultOfficeManagerConfiguration defaultOfficeManagerConfiguration=new DefaultOfficeManagerConfiguration();
		if(openOfficeHome!=null){
			officeManager = defaultOfficeManagerConfiguration.setOfficeHome(openOfficeHome).buildOfficeManager();
		}else{
			officeManager = new DefaultOfficeManagerConfiguration().buildOfficeManager();
		}		
		ServiceResourceHolder.setOfficeManager(officeManager);
		officeManager.start();
		
		boolean enableActivityEngineCache=false;
		String serviceApplicationGlobalConfigFile=RuntimeEnvironmentHandler.getApplicationRootPath()+"ServiceApplicationGlobalCfg.properties";		
		Properties _globalConfigProperties=new Properties();
		try {
			_globalConfigProperties.load(new FileInputStream(serviceApplicationGlobalConfigFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}				
		String enableActivityEngineCacheFlag=_globalConfigProperties.getProperty("ENABLE_ACTIVITYENGINE_CACHE");
		enableActivityEngineCache=Boolean.parseBoolean(enableActivityEngineCacheFlag);
		if(enableActivityEngineCache){
			ActivityEngineCache activityEngineCache=new ActivityEngineCacheUtil().initActivityEngineCache();
			ActivityComponentFactory.setActivityEngineCache(activityEngineCache);
			ServiceResourceHolder.setActivityEngineCache(activityEngineCache);
		}
	}
}