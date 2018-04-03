package com.viewfunction.businessActivityManagementService.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

public class ApplicationLauncherUtil {

    private static Properties _applicationInfoProperties;

    public static String getApplicationInfoPropertyValue(String resourceFileName){
        if(_applicationInfoProperties==null){
            _applicationInfoProperties=new Properties();
            try {
                _applicationInfoProperties.load(new FileInputStream(RuntimeEnvironmentHandler.getApplicationRootPath()+"/appConfig/applicationInfo.properties"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return _applicationInfoProperties.getProperty(resourceFileName);
    }

    public static void printApplicationConsoleBanner(){
        StringBuffer bannerStringBuffer=new StringBuffer();
        String echo0="==========================================================";
        String echo1="       _            ___              __  _         ";
        String echo2=" _  __(_)__ _    __/ _/_ _____  ____/ /_(_)__  ___ ";
        String echo3="| |/ / / -_) |/|/ / _/ // / _ \\/ __/ __/ / _ \\/ _ \\";
        String echo4="|___/_/\\__/|__,__/_/ \\_,_/_//_/\\__/\\__/_/\\___/_//_/";
        String echo5="---------------------------------------------------------";
        bannerStringBuffer.append(echo0);
        bannerStringBuffer.append("\n\r");
        bannerStringBuffer.append(echo1);
        bannerStringBuffer.append("\n\r");
        bannerStringBuffer.append(echo2);
        bannerStringBuffer.append("\n\r");
        bannerStringBuffer.append(echo3);
        bannerStringBuffer.append("\n\r");
        bannerStringBuffer.append(echo4);
        bannerStringBuffer.append("\n\r");
        bannerStringBuffer.append("\n\r");
        bannerStringBuffer.append(echo5);
        bannerStringBuffer.append("\n\r");

        bannerStringBuffer.append(getApplicationInfoPropertyValue("applicationName")+"\n\r"
                +"ver. " + getApplicationInfoPropertyValue("applicationVersion"));

        bannerStringBuffer.append("\n\r");
        String echo6="---------------------------------------------------------";
        bannerStringBuffer.append(echo6);
        bannerStringBuffer.append("\n\r");
        bannerStringBuffer.append("\n\r");

        Properties prop = System.getProperties();
        String osName = prop.getProperty("os.name");
        String osVersion = prop.getProperty("os.version");
        String osArch = prop.getProperty("os.arch");
        String osInfo=osName+" "+osVersion+" "+osArch;
        bannerStringBuffer.append("OS: "+osInfo);
        bannerStringBuffer.append("\n\r");

        bannerStringBuffer.append("User: "+prop.getProperty("user.name"));
        bannerStringBuffer.append("\n\r");

        String jvmVendor=prop.getProperty("java.vm.vendor");
        String jvmName=prop.getProperty("java.vm.name");
        String jvmVersion=prop.getProperty("java.vm.version");
        bannerStringBuffer.append("JVM: ver. "+prop.getProperty("java.version")+ " "+jvmVendor);
        bannerStringBuffer.append("\n\r");
        bannerStringBuffer.append("     "+jvmName +" "+jvmVersion);
        bannerStringBuffer.append("\n\r");

        bannerStringBuffer.append("Started at: "+new Date()+"");
        bannerStringBuffer.append("\n\r");
        String echo7="==========================================================";
        bannerStringBuffer.append(echo7);
        System.out.println(bannerStringBuffer.toString());
    }
}
