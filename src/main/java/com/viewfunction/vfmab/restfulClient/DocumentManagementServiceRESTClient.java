package com.viewfunction.vfmab.restfulClient;

import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;

import com.viewfunction.vfmab.restful.documentManagement.DocumentConvertOperationVO;
import com.viewfunction.vfmab.restful.util.BooleanOperationResultVO;

public class DocumentManagementServiceRESTClient {		
	public static BooleanOperationResultVO convertOfficeDocumentToPDF(DocumentConvertOperationVO documentConvertOperationVO){
		WebClient client = WebClient.create(RESTClientConfigUtil.getREST_baseURLValue());
		client.path("documentManagementService/convertOfficeDocumentToPDF/");		
		client.type("application/xml").accept("application/xml");		
		Response response = client.post(documentConvertOperationVO);		
		BooleanOperationResultVO booleanOperationResultVO=response.readEntity(BooleanOperationResultVO.class);		
		return booleanOperationResultVO;
	}	
	
	public static void main(String[] args){
		DocumentConvertOperationVO documentConvertOperationVO=new DocumentConvertOperationVO();
		documentConvertOperationVO.setSourceDocumentPath("E://Î´ÃüÃû 1.odp");
		documentConvertOperationVO.setTargetDocumentPath("E://");
		BooleanOperationResultVO result=DocumentManagementServiceRESTClient.convertOfficeDocumentToPDF(documentConvertOperationVO);
		System.out.println(result.isOperationResult());
	}
}