package com.viewfunction.vfmab.restful.util;

import org.artofsolving.jodconverter.office.OfficeManager;

import com.viewfunction.activityEngine.util.cache.ActivityEngineCache;

public class ServiceResourceHolder {
	private static OfficeManager officeManager;
	
	private static ActivityEngineCache activityEngineCache;

	public static OfficeManager getOfficeManager() {
		return officeManager;
	}

	public static void setOfficeManager(OfficeManager officeManager) {
		ServiceResourceHolder.officeManager = officeManager;
	}

	public static ActivityEngineCache getActivityEngineCache() {
		return activityEngineCache;
	}

	public static void setActivityEngineCache(ActivityEngineCache activityEngineCache) {
		ServiceResourceHolder.activityEngineCache = activityEngineCache;
	}

}
