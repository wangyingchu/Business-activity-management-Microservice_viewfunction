package com.viewfunction.vfmab.restful.documentManagement;

import java.io.File;
import java.util.Date;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.artofsolving.jodconverter.OfficeDocumentConverter;

import com.viewfunction.vfmab.restful.util.BooleanOperationResultVO;
import com.viewfunction.vfmab.restful.util.ServiceResourceHolder;
import org.springframework.stereotype.Service;

@Service
@Path("/documentManagementService")  
@Produces("application/json")
public class DocumentManagementService {	
	@POST
    @Path("/convertOfficeDocumentToPDF/")	
	@Produces("application/xml")
	public BooleanOperationResultVO convertOfficeDocumentToPDF(DocumentConvertOperationVO documentConvertOperationVO){		
		BooleanOperationResultVO operationBooleanResultVO=new BooleanOperationResultVO();
		operationBooleanResultVO.setOperationResult(false);
		String sourceOfficeDocumentPath=documentConvertOperationVO.getSourceDocumentPath();
		String targetPDFDocumentPath=documentConvertOperationVO.getTargetDocumentPath();		
		OfficeDocumentConverter converter = new OfficeDocumentConverter(ServiceResourceHolder.getOfficeManager());	
		File sourceOfficeDocument=new File(sourceOfficeDocumentPath);
		File targetPDFDocument=new File(targetPDFDocumentPath);					
		converter.convert(sourceOfficeDocument, targetPDFDocument);
		operationBooleanResultVO.setOperationResult(true);
		operationBooleanResultVO.setTiemStamp(new Date().getTime());
		return operationBooleanResultVO;
	}	
}