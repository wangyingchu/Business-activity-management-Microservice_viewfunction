package com.viewfunction.vfmab.restfulClient;

import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;

import com.viewfunction.vfmab.restful.userManagement.UserBasicInfoVO;
import com.viewfunction.vfmab.restful.userManagement.UserBasicInfoVOList;
import com.viewfunction.vfmab.restful.util.BooleanOperationResultVO;

public class UserManagementServiceRESTClient {	
	public static UserBasicInfoVOList getUserUnitsInfoOfRole(String applicationSpaceName,String roleName){
		WebClient client = WebClient.create(RESTClientConfigUtil.getREST_baseURLValue());
		client.path("userManagementService/usersInfoOfRole/"+applicationSpaceName+"/"+roleName+"/");		
		client.type("application/xml").accept("application/xml");
		Response response =client.get();		
		UserBasicInfoVOList userBasicInfoVOList= response.readEntity(UserBasicInfoVOList.class);
		return userBasicInfoVOList;
	}	
	
	public static BooleanOperationResultVO syncAddNewParticipant(String applicationSpaceName,UserBasicInfoVO newUserBasicInfoVO){
		WebClient client = WebClient.create(RESTClientConfigUtil.getREST_baseURLValue());
		client.path("userManagementService/syncNewParticipant/"+applicationSpaceName+"/");		
		client.type("application/xml").accept("application/xml");		
		Response response = client.post(newUserBasicInfoVO);		
		BooleanOperationResultVO booleanOperationResultVO=response.readEntity(BooleanOperationResultVO.class);		
		return booleanOperationResultVO;
	}	
	
	public static void main(String[] args){
		UserBasicInfoVOList userBasicInfoVOList=UserManagementServiceRESTClient.getUserUnitsInfoOfRole("aaaa", "Manufacturing Department");
		System.out.println(userBasicInfoVOList.getUserBasicInfoVOList());
	}
}