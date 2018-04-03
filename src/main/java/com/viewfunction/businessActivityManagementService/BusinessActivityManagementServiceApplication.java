package com.viewfunction.businessActivityManagementService;

import com.viewfunction.businessActivityManagementService.util.ApplicationLauncherUtil;
import com.viewfunction.vfmab.restful.activityManagement.ActivityManagementService;
import com.viewfunction.vfmab.restful.commentManagement.CommentManagementService;
import com.viewfunction.vfmab.restful.contentManagement.ContentManagementService;
import com.viewfunction.vfmab.restful.documentManagement.DocumentManagementService;
import com.viewfunction.vfmab.restful.userManagement.UserManagementService;
import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.provider.JAXBElementProvider;
import org.apache.cxf.jaxrs.swagger.Swagger2Feature;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.util.Arrays;

@SpringBootApplication
@ServletComponentScan(basePackages = { "com.viewfunction.vfmab"})
@ComponentScan(basePackages = { "com.viewfunction.vfmab"})
@EnableDiscoveryClient
public class BusinessActivityManagementServiceApplication {

    @Autowired
    private Bus cxfBus;

    @Autowired
    private ContentManagementService contentManagementService;

    @Autowired
    private DocumentManagementService documentManagementService;

    @Autowired
    private UserManagementService userManagementService;

    @Autowired
    private ActivityManagementService activityManagementService;

    @Autowired
    private CommentManagementService commentManagementService;

    @Bean
    public JacksonJaxbJsonProvider jacksonJaxbJsonProvider() {
        return new JacksonJaxbJsonProvider();
    }

    @Bean
    public JAXBElementProvider jaxbElementProvider() {
        return new JAXBElementProvider();
    }

    @Bean
    public Server cxfRestFulServer() {
        JAXRSServerFactoryBean endpoint = new JAXRSServerFactoryBean();
        endpoint.setBus(cxfBus);
        endpoint.setAddress("/businessActivityManagement");
        endpoint.setServiceBeans(Arrays.asList(contentManagementService,documentManagementService,userManagementService,activityManagementService,commentManagementService));
        endpoint.setProviders(Arrays.asList(jacksonJaxbJsonProvider(), jaxbElementProvider()));
        endpoint.setFeatures(Arrays.asList(new Swagger2Feature()));
        return endpoint.create();
    }

    public static void main(String[] args) {
		SpringApplication.run(BusinessActivityManagementServiceApplication.class, args);
        ApplicationLauncherUtil.printApplicationConsoleBanner();
	}
}
