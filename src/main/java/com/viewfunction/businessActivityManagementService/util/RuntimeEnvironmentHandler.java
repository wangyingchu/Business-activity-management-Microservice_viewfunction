package com.viewfunction.businessActivityManagementService.util;

public class RuntimeEnvironmentHandler {

    private static String APPLICATION_ROOT_PATH;

	public static String getApplicationRootPath() {
	    if(APPLICATION_ROOT_PATH==null){
            String runtimeClassLocation=RuntimeEnvironmentHandler.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            if(runtimeClassLocation.endsWith("/BOOT-INF/classes!/")){
                //in spring boot jar mode
                int folderIndex=runtimeClassLocation.indexOf("runtime/");
                String applicationRootPath=runtimeClassLocation.substring(0,folderIndex).replace("file:","");
                APPLICATION_ROOT_PATH= applicationRootPath+"runtime/";
            }
            if(runtimeClassLocation.endsWith("/target/classes/")){
                //in development env mode
                String applicationRootPath=runtimeClassLocation.replace("target/classes/","");
                APPLICATION_ROOT_PATH= applicationRootPath+"runtime/";
            }
        }
	    return APPLICATION_ROOT_PATH;
   }
}