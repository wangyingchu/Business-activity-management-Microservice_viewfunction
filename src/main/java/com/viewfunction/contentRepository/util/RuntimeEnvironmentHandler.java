package com.viewfunction.contentRepository.util;

public class RuntimeEnvironmentHandler {
	
        public static String getApplicationRootPath() { 
             if(System.getProperty("catalina.home")!=null){
                // running under tomcat container, set application root location to /WEB-INF
                StringBuilder sb = new StringBuilder(PerportyHandler.class.getResource("").toString());
                String OSType = System.getProperty("os.name");
                if (OSType.startsWith("Windows")) {
                    sb.delete(0, 6); //this only work on windows system
                } else {
                    sb.delete(0, 5);//this only work on Unix system
                }
                sb.reverse().delete(0, 48).reverse();
                return sb.toString().replaceAll("%20", " ");
             }else{
                 //running under common standalone mode,set application root location to ./
                return "";
             }
        }
}